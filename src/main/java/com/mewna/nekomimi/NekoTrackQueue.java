package com.mewna.nekomimi;

import java.util.Collection;
import java.util.Deque;
import java.util.concurrent.ConcurrentLinkedDeque;

/**
 * @author amy
 * @since 10/31/18.
 */
public class NekoTrackQueue {
    private final String guildId;
    
    // TODO: Back this with redis
    private Deque<NekoTrack> queue = new ConcurrentLinkedDeque<>();
    
    public NekoTrackQueue(final String guildId) {
        this.guildId = guildId;
    }
    
    public void loadTracks(final Collection<NekoTrack> tracks) {
        queue.addAll(tracks);
    }
    
    public NekoTrack nextTrack() {
        return queue.pollFirst();
    }
    
    public boolean hasNext() {
        return !queue.isEmpty();
    }
}
