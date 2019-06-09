package com.mewna.nekomimi.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 6/9/19.
 */
@Value
@Accessors(fluent = true)
public class VoiceSkip {
    @JsonProperty("type")
    private final String type;
    @JsonProperty("guild_id")
    private final String guildId;
}
