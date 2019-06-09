package com.mewna.nekomimi.api;

import com.mewna.nekomimi.Nekomimi;
import com.mewna.nekomimi.track.NekoTrack;
import com.mewna.nekomimi.track.NekoTrackContext;
import com.mewna.nekomimi.track.NekoTrackEvent;
import com.mewna.nekomimi.track.NekoTrackEvent.TrackEventType;
import com.mewna.nekomimi.track.NekoTrackQueue;
import io.vertx.core.http.HttpServer;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

/**
 * @author amy
 * @since 6/9/19.
 */
@RequiredArgsConstructor
public final class Api {
    private final Nekomimi nekomimi;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    public void setup() {
        logger.info("Starting API server...");
        final HttpServer server = nekomimi.vertx().createHttpServer();
        final Router router = Router.router(nekomimi.vertx());
        
        router.post("/api/np/:guild").handler(BodyHandler.create()).handler(ctx -> {
            final String guild = ctx.request().getParam("guild");
            final NekoTrackQueue queue = nekomimi.queues().computeIfAbsent(guild, __ -> new NekoTrackQueue(guild));
            final NekoTrackContext nctx = ctx.getBodyAsJson().mapTo(NekoTrackContext.class);
            NekoTrack currentTrack = queue.currentTrack();
            if(queue.currentAudioTrack() != null) {
                // Backfill a bit of data
                currentTrack = currentTrack.toBuilder()
                        .position(queue.currentAudioTrack().getPosition())
                        .context(nctx)
                        .build();
            } else {
                currentTrack = NekoTrack.builder()
                        .context(nctx)
                        .build();
            }
            ctx.response().write(new NekoTrackEvent(TrackEventType.AUDIO_TRACK_NOW_PLAYING, currentTrack).toJson().encode());
        });
        
        server.requestHandler(router).listen(Integer.parseInt(Optional.ofNullable(System.getenv("PORT"))
                .orElse("12345")));
        logger.info("API started!");
    }
}
