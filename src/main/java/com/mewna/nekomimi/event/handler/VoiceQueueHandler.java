package com.mewna.nekomimi.event.handler;

import com.mewna.nekomimi.Nekomimi;
import com.mewna.nekomimi.event.VoiceQueue;
import com.mewna.nekomimi.track.NekoTrackContext;
import com.mewna.nekomimi.track.NekoTrackLoader;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * @author amy
 * @since 6/9/19.
 */
@RequiredArgsConstructor
public class VoiceQueueHandler implements Consumer<VoiceQueue> {
    private final Nekomimi nekomimi;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void accept(final VoiceQueue voiceQueue) {
        final NekoTrackContext ctx = voiceQueue.context();
        if(voiceQueue.search() != null) {
            final String search = voiceQueue.search();
            logger.debug("Got search queue: " + search);
            nekomimi.playerManager().loadItem("ytsearch:" + search,
                    new NekoTrackLoader(nekomimi, ctx, true));
        } else {
            final String url = voiceQueue.url();
            logger.debug("Got url queue: " + url);
            nekomimi.playerManager().loadItem(url,
                    new NekoTrackLoader(nekomimi, ctx, false));
        }
    }
}
