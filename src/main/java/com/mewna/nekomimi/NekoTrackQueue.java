package com.mewna.nekomimi;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author amy
 * @since 10/31/18.
 */
@Accessors(fluent = true)
@SuppressWarnings("WeakerAccess")
public class NekoTrackQueue {
    @Getter
    @SuppressWarnings({"FieldCanBeLocal", "unused"})
    private final String guildId;
    // TODO: Back this with redis
    private final Deque<NekoTrack> queue = new ConcurrentLinkedDeque<>();
    @Getter
    private NekoTrack currentTrack;
    @Getter
    @Setter
    private AudioTrack currentAudioTrack;
    @Getter
    private String lastChannel;
    
    public NekoTrackQueue(final String guildId) {
        this.guildId = guildId;
    }
    
    public void loadTracks(final Collection<NekoTrack> tracks) {
        queue.addAll(tracks);
        lastChannel = new ArrayList<>(tracks).get(tracks.size() - 1).context().channel();
    }
    
    public NekoTrack nextTrack() {
        currentTrack = queue.pollFirst();
        return currentTrack;
    }
    
    public boolean hasNext() {
        if(queue.isEmpty()) {
            currentTrack = null;
        }
        return !queue.isEmpty();
    }
    
    public long countTracks() {
        return queue.size();
    }
}
