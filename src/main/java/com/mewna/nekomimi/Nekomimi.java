package com.mewna.nekomimi;

import com.mewna.nekomimi.api.Api;
import com.mewna.nekomimi.handler.*;
import com.mewna.nekomimi.message.*;
import com.mewna.nekomimi.player.NekoPlayerLoader;
import com.mewna.nekomimi.track.NekoTrack;
import com.mewna.nekomimi.track.NekoTrackEvent;
import com.mewna.nekomimi.track.NekoTrackEvent.TrackEventType;
import com.mewna.nekomimi.track.NekoTrackQueue;
import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.timgroup.statsd.NoOpStatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import gg.amy.singyeong.SingyeongClient;
import gg.amy.singyeong.client.query.QueryBuilder;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.npstr.magma.MagmaApi;
import space.npstr.magma.MagmaMember;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

/**
 * @author amy
 * @since 10/13/18.
 */
@Accessors(fluent = true)
public final class Nekomimi {
    public static final String USER_ID = System.getenv("CLIENT_ID");
    @Getter
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    @Getter
    private final Vertx vertx = Vertx.vertx();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Getter
    private final Set<String> guilds = new HashSet<>();
    @Getter
    private final Map<String, NekoTrackQueue> queues = new ConcurrentHashMap<>();
    @Getter
    private final StatsDClient statsClient;
    private final Map<Class<?>, Consumer<?>> handlers = new HashMap<>() {{
        put(VoiceJoin.class, new VoiceJoinHandler(Nekomimi.this));
        put(VoiceLeave.class, new VoiceLeaveHandler(Nekomimi.this));
        put(VoicePlay.class, new VoicePlayHandler(Nekomimi.this));
        put(VoiceQueue.class, new VoiceQueueHandler(Nekomimi.this));
        put(VoiceSkip.class, new VoiceSkipHandler(Nekomimi.this));
    }};
    private final Map<String, Class<?>> typeMap = new HashMap<>() {{
        put("VOICE_JOIN", VoiceJoin.class);
        put("VOICE_LEAVE", VoiceLeave.class);
        put("VOICE_PLAY", VoicePlay.class);
        put("VOICE_QUEUE", VoiceQueue.class);
        put("VOICE_SKIP", VoiceSkip.class);
    }};
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
    
    @SuppressWarnings("unchecked")
    private <T> Consumer<T> handler(final Class<T> cls) {
        return (Consumer<T>) handlers.get(cls);
    }
    
    public void loadTracks(final String guildId, final Collection<NekoTrack> tracks) {
        queues.computeIfAbsent(guildId, __ -> new NekoTrackQueue(guildId)).loadTracks(tracks);
    }
    
    private <T> void handleEvent(final String type, final JsonObject payload) {
        @SuppressWarnings("unchecked")
        final Class<T> eventClass = (Class<T>) typeMap.get(type);
        final T event = payload.mapTo(eventClass);
        handler(eventClass).accept(event);
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
            final String type = payload.getString("type");
            handleEvent(type, payload);
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
