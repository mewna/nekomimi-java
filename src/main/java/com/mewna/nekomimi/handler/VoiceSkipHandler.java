package com.mewna.nekomimi.handler;

import com.mewna.nekomimi.Nekomimi;
import com.mewna.nekomimi.message.VoiceSkip;
import com.mewna.nekomimi.track.NekoTrackQueue;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * @author amy
 * @since 6/9/19.
 */
@RequiredArgsConstructor
public class VoiceSkipHandler implements Consumer<VoiceSkip> {
    private final Nekomimi nekomimi;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void accept(final VoiceSkip voiceSkip) {
        final NekoTrackQueue queue = nekomimi.queues().get(voiceSkip.guildId());
        if(queue.currentAudioTrack() != null) {
            nekomimi.playNextInQueue(voiceSkip.guildId());
        }
    }
}
