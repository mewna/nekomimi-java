package com.mewna.nekomimi;

import com.mewna.nekomimi.api.Api;
import com.mewna.nekomimi.message.*;
import com.mewna.nekomimi.player.NekoPlayerLoader;
import com.mewna.nekomimi.track.*;
import com.mewna.nekomimi.track.NekoTrackEvent.TrackEventType;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import gg.amy.singyeong.SingyeongClient;
import gg.amy.singyeong.client.SingyeongType;
import gg.amy.singyeong.client.query.QueryBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.npstr.magma.*;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author amy
 * @since 10/13/18.
 */
@Accessors(fluent = true)
public final class Nekomimi {
    private static final String USER_ID = System.getenv("CLIENT_ID");
    @Getter
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    @Getter
    private final Vertx vertx = Vertx.vertx();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Set<String> guilds = new HashSet<>();
    @Getter
    private final Map<String, NekoTrackQueue> queues = new ConcurrentHashMap<>();
    @Getter
    private final StatsDClient statsClient;
    @Getter
    private MagmaApi magma;
    @Getter
    private SingyeongClient singyeong;
    
    private Nekomimi() {
        if(System.getenv("STATSD_ENABLED") != null) {
            statsClient = new NonBlockingStatsDClient("nekomimi", System.getenv("STATSD_HOST"), 8125);
        } else {
            statsClient = new NoOpStatsDClient();
        }
    }
    
    public static void main(final String[] args) {
        new Nekomimi().start();
    }
    
    public void loadTracks(final String guildId, final Collection<NekoTrack> tracks) {
        queues.computeIfAbsent(guildId, __ -> new NekoTrackQueue(guildId)).loadTracks(tracks);
    }
    
    private void start() {
        AudioSourceManagers.registerRemoteSources(playerManager);
        magma = MagmaApi.of(__ -> new NativeAudioSendFactory());
        
        singyeong = SingyeongClient.create(vertx, System.getenv("SINGYEONG_DSN"));
        
        vertx.setPeriodic(10_000L, __ -> {
            statsClient.gauge("activeVcs", guilds.size());
            statsClient.gauge("playingTracks", queues.values().stream()
                    .filter(e -> e.currentAudioTrack() != null).count());
            statsClient.gauge("loadedTracks", queues.values().stream()
                    .mapToLong(NekoTrackQueue::countTracks).sum());
        });
        
        singyeong.onEvent(dispatch -> {
            final JsonObject payload = dispatch.data();
            switch(payload.getString("type")) {
                case "VOICE_JOIN": {
                    final var voiceJoin = payload.mapTo(VoiceJoin.class);
                    final Member member = MagmaMember.builder()
                            .guildId(voiceJoin.guildId())
                            .userId(USER_ID)
                            .build();
                    final ServerUpdate serverUpdate = MagmaServerUpdate.builder()
                            .sessionId(voiceJoin.sessionId())
                            .endpoint(voiceJoin.endpoint())
                            .token(voiceJoin.token())
                            .build();
                    
                    logger.info("Got voice join to {} {}", member, serverUpdate);
                    magma.provideVoiceServerUpdate(member, serverUpdate);
                    
                    guilds.add(voiceJoin.guildId());
                    singyeong.updateMetadata("guilds", SingyeongType.LIST, new JsonArray(new ArrayList<>(guilds)));
                    
                    statsClient.gauge("activeVcs", guilds.size());
                    
                    queues.putIfAbsent(voiceJoin.guildId(), new NekoTrackQueue(voiceJoin.guildId()));
                    break;
                }
                case "VOICE_LEAVE": {
                    final var voiceLeave = payload.mapTo(VoiceLeave.class);
                    final Member member = MagmaMember.builder()
                            .guildId(voiceLeave.guildId())
                            .userId(USER_ID)
                            .build();
                    logger.info("Got voice leave for {}", member);
                    magma.removeSendHandler(member);
                    magma.closeConnection(member);
                    guilds.remove(voiceLeave.guildId());
                    statsClient.gauge("activeVcs", guilds.size());
                    singyeong.updateMetadata("guilds", SingyeongType.LIST, new JsonArray(new ArrayList<>(guilds)));
                    break;
                }
                case "VOICE_QUEUE": {
                    final var voiceQueue = payload.mapTo(VoiceQueue.class);
                    final NekoTrackContext ctx = voiceQueue.context();
                    if(voiceQueue.search() != null) {
                        final String search = voiceQueue.search();
                        logger.debug("Got search queue: " + search);
                        playerManager.loadItem("ytsearch:" + search, new NekoTrackLoader(this, ctx, true));
                    } else {
                        final String url = voiceQueue.url();
                        logger.debug("Got url queue: " + url);
                        playerManager.loadItem(url, new NekoTrackLoader(this, ctx, false));
                    }
                    break;
                }
                case "VOICE_PLAY": {
                    final var voicePlay = payload.mapTo(VoicePlay.class);
                    playNextInQueue(voicePlay.guildId());
                    break;
                }
                case "VOICE_SKIP": {
                    final var voiceSkip = payload.mapTo(VoiceSkip.class);
                    final NekoTrackQueue queue = queues.get(voiceSkip.guildId());
                    if(queue.currentAudioTrack() != null) {
                        playNextInQueue(voiceSkip.guildId());
                    }
                    break;
                }
                default: {
                    logger.warn("UNKNOWN EVENT: {}", payload.getString("type"));
                    break;
                }
            }
        });
        
        new Api(this).setup();
        singyeong.connect()
                .thenAccept(__ -> logger.info("Welcome to nekomimi!"));
    }
    
    public NekoTrackQueue queue(final String guildId) {
        return queues.putIfAbsent(guildId, new NekoTrackQueue(guildId));
    }
    
    public void playNextInQueue(final String guildId) {
        statsClient.gauge("playingTracks", queues.values().stream()
                .filter(e -> e.currentAudioTrack() != null).count());
        if(!guilds.contains(guildId)) {
            return;
        }
        final NekoTrackQueue queue = queues.get(guildId);
        if(queue.currentAudioTrack() != null) {
            if(queue.currentPlayer() != null) {
                queue.currentPlayer().removeListener(queue.currentListener());
            }
            queue.currentAudioTrack().stop();
            magma.removeSendHandler(MagmaMember.builder().userId(USER_ID).guildId(guildId).build());
        }
        // we fetch it here since calling hasNext() will null it
        final NekoTrack currentTrack = queue.currentTrack();
        if(queue.hasNext()) {
            final NekoTrack track = queue.nextTrack();
            playerManager.loadItem(track.url(), new NekoPlayerLoader(this, track));
        } else if(currentTrack != null) {
            singyeong.send(new QueryBuilder().target("backend").build(),
                    new JsonObject()
                            .put("type", TrackEventType.AUDIO_QUEUE_END.name())
                            .put("data", new NekoTrackEvent(TrackEventType.AUDIO_QUEUE_END,
                                    new NekoTrack(null, null, null, 0L, 0L,
                                            currentTrack.context()))
                                    .toJson()));
        }
    }
}
