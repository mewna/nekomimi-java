package com.mewna.nekomimi.event.handler;

import com.mewna.nekomimi.Nekomimi;
import com.mewna.nekomimi.event.VoicePlay;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

/**
 * @author amy
 * @since 6/9/19.
 */
@RequiredArgsConstructor
public class VoicePlayHandler implements Consumer<VoicePlay> {
    private final Nekomimi nekomimi;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void accept(final VoicePlay voicePlay) {
        nekomimi.playNextInQueue(voicePlay.guildId());
    }
}
