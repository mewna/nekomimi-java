package com.mewna.nekomimi;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.npstr.magma.MagmaMember;

/**
 * @author amy
 * @since 11/2/18.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class NekoPlayerLoader implements AudioLoadResultHandler {
    private final Nekomimi nekomimi;
    private final NekoTrack track;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void trackLoaded(final AudioTrack track) {
        final AudioPlayer player = nekomimi.playerManager().createPlayer();
        final NekoPlayerListener listener = new NekoPlayerListener(nekomimi, this.track);
        player.addListener(listener);
        nekomimi.magma().setSendHandler(MagmaMember.builder()
                        .userId(System.getenv("CLIENT_ID"))
                        .guildId(this.track.context().guild()).build(),
                new NekoSendHandler(player));
        player.playTrack(track);
        nekomimi.queue(this.track.context().guild()).currentPlayer(player);
        nekomimi.queue(this.track.context().guild()).currentListener(listener);
    }
    
    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        logger.warn("Playlists not implemented!");
    }
    
    @Override
    public void noMatches() {
        logger.error("No matches for track {}", track);
    }
    
    @Override
    public void loadFailed(final FriendlyException exception) {
        logger.error("Load failed for track {}", track, exception);
    }
}
