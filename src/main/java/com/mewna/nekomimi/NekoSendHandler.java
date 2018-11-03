package com.mewna.nekomimi;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.dv8tion.jda.core.audio.AudioSendHandler;

/**
 * @author amy
 * @since 10/28/18.
 */
public class NekoSendHandler implements AudioSendHandler {
    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;
    
    public NekoSendHandler(final AudioPlayer audioPlayer) {
        this.audioPlayer = audioPlayer;
    }
    
    @Override
    public boolean canProvide() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }
    
    @Override
    public byte[] provide20MsAudio() {
        return lastFrame.getData();
    }
    
    @Override
    public boolean isOpus() {
        return true;
    }
}