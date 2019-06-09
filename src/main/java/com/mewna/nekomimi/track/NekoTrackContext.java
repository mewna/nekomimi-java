package com.mewna.nekomimi.track;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 11/2/18.
 */
@Getter(onMethod_ = {@JsonProperty})
@Setter(onMethod_ = {@JsonProperty})
@Accessors(fluent = true)
@SuppressWarnings("unused")
public final class NekoTrackContext {
    @JsonProperty
    private String user;
    @JsonProperty
    private String channel;
    @JsonProperty
    private String guild;
    
    public NekoTrackContext() {
    }
    
    public NekoTrackContext(final String user, final String channel, final String guild) {
        this.user = user;
        this.channel = channel;
        this.guild = guild;
    }
}
