package com.mewna.nekomimi.track;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 10/31/18.
 */
@Getter(onMethod_ = {@JsonProperty})
@Builder(toBuilder = true)
@Accessors(fluent = true)
@AllArgsConstructor
public class NekoTrack {
    /**
     * URL used to load the track
     */
    @JsonProperty
    private final String url;
    
    /**
     * Track title (given by lavaplayer)
     */
    @JsonProperty
    private final String title;
    
    /**
     * Track author (given by lavaplayer)
     */
    @JsonProperty
    private final String author;
    
    /**
     * Track length, in milliseconds (given by lavaplayer)
     */
    @JsonProperty
    private final long length;
    
    /**
     * Current position of the track being played, in milliseconds
     */
    @JsonProperty
    private final long position;
    
    /**
     * Context of the track - who requested it, what channel it was requested
     * in, ...
     */
    @JsonProperty
    private final NekoTrackContext context;
}
