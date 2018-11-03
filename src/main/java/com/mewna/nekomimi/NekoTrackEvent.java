package com.mewna.nekomimi;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 11/2/18.
 */
@Getter
@Accessors(fluent = true)
public class NekoTrackEvent {
    @JsonProperty
    private final TrackEventType type;
    @JsonProperty
    private final NekoTrack track;
    @JsonProperty
    private final long ts = System.currentTimeMillis();
    
    public NekoTrackEvent(final TrackEventType type, final NekoTrack track) {
        this.type = type;
        this.track = track;
    }

    public JsonObject toJson() {
        return JsonObject.mapFrom(this);
    }
    
    public enum TrackEventType {
        /**
         * Track started playing
         */
        AUDIO_TRACK_START,
        /**
         * Track stopped playing
         */
        AUDIO_TRACK_STOP,
        /**
         * Track was paused
         */
        AUDIO_TRACK_PAUSE,
        /**
         * Track was queued
         */
        AUDIO_TRACK_QUEUE,
        /**
         * Track was invalid
         */
        AUDIO_TRACK_INVALID,
        /**
         * Request to fetch current track
         */
        AUDIO_TRACK_NOW_PLAYING,
        /**
         * Queue ended
         */
        AUDIO_QUEUE_END,
    }
}
