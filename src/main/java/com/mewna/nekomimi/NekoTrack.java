package com.mewna.nekomimi;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

import java.beans.Transient;

/**
 * @author amy
 * @since 10/31/18.
 */
@Getter(onMethod_ = {@JsonProperty})
@Accessors(fluent = true)
@SuppressWarnings("WeakerAccess")
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
    
    @Transient
    @JsonIgnore
    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
}
