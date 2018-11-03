package com.mewna.nekomimi;

import com.sedmelluq.discord.lavaplayer.jdaudp.NativeAudioSendFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import gg.amy.singyeong.SingyeongClient;
import gg.amy.singyeong.SingyeongType;
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
    private final Vertx vertx = Vertx.vertx();
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Set<String> guilds = new HashSet<>();
    private final Map<String, NekoTrackQueue> queues = new ConcurrentHashMap<>();
    @Getter
    private MagmaApi magma;
    @Getter
    private SingyeongClient singyeong;
    
    private Nekomimi() {
    }
    
    public static void main(final String[] args) {
        new Nekomimi().start();
    }
    
    @SuppressWarnings("WeakerAccess")
    public void loadTracks(final String guildId, final Collection<NekoTrack> tracks) {
        queues.computeIfAbsent(guildId, __ -> new NekoTrackQueue(guildId)).loadTracks(tracks);
    }
    
    private void start() {
        AudioSourceManagers.registerRemoteSources(playerManager);
        magma = MagmaApi.of(__ -> new NativeAudioSendFactory());
        
        singyeong = new SingyeongClient(System.getenv("SINGYEONG_DSN"), vertx, "nekomimi");
        
        singyeong.onEvent(dispatch -> {
            final JsonObject payload = dispatch.data();
            switch(payload.getString("type")) {
                case "VOICE_JOIN": {
                    final String guildId = payload.getString("guild_id");
                    final Member member = MagmaMember.builder()
                            .guildId(guildId)
                            .userId(USER_ID)
                            .build();
                    final ServerUpdate serverUpdate = MagmaServerUpdate.builder()
                            .sessionId(payload.getString("session_id"))
                            .endpoint(payload.getString("endpoint"))
                            .token(payload.getString("token"))
                            .build();
                    
                    logger.info("Got voice join to {} {}", member, serverUpdate);
                    magma.provideVoiceServerUpdate(member, serverUpdate);
                    
                    guilds.add(guildId);
                    singyeong.updateMetadata("guilds", SingyeongType.LIST, new JsonArray(new ArrayList<>(guilds)));
                    
                    queues.putIfAbsent(guildId, new NekoTrackQueue(guildId));
                    break;
                }
                case "VOICE_LEAVE": {
                    final String guildId = payload.getString("guild_id");
                    final Member member = MagmaMember.builder()
                            .guildId(guildId)
                            .userId(USER_ID)
                            .build();
                    logger.info("Got voice leave for {}", member);
                    magma.removeSendHandler(member);
                    magma.closeConnection(member);
                    guilds.remove(guildId);
                    singyeong.updateMetadata("guilds", SingyeongType.LIST, new JsonArray(new ArrayList<>(guilds)));
                    break;
                }
                case "VOICE_QUEUE": {
                    final String url = payload.getString("url");
                    final NekoTrackContext ctx = payload.getJsonObject("context").mapTo(NekoTrackContext.class);
                    // TODO: Differentiate between url and query
                    playerManager.loadItem(url, new NekoTrackLoader(this, ctx));
                    break;
                }
                case "VOICE_PLAY": {
                    final String guildId = payload.getString("guild_id");
                    playNextInQueue(guildId);
                    
                    break;
                }
                default: {
                    logger.warn("UNKNOWN EVENT: {}", payload.getString("type"));
                    break;
                }
            }
        });
        
        singyeong.connect()
                .thenAccept(__ -> logger.info("Welcome to nekomimi!"));
    }
    
    @SuppressWarnings("WeakerAccess")
    public void playNextInQueue(final String guildId) {
        final NekoTrackQueue queue = queues.get(guildId);
        
        if(queue.hasNext()) {
            final NekoTrack track = queue.nextTrack();
            playerManager.loadItem(track.url(), new NekoPlayerLoader(this, track));
        } else {
            // TODO: Emit
            logger.warn("Queue empty for guild {}", guildId);
        }
    }
}
