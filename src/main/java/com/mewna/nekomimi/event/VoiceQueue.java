package com.mewna.nekomimi.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.mewna.nekomimi.track.NekoTrackContext;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 6/9/19.
 */
@Value
@Accessors(fluent = true)
public class VoiceQueue {
    @JsonProperty("type")
    private final String type;
    @JsonProperty("url")
    private final String url;
    @JsonProperty("context")
    private final NekoTrackContext context;
    @JsonProperty("search")
    private final String search;
}
