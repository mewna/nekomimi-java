package com.mewna.nekomimi.message;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Value;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 6/9/19.
 */
@Value
@Accessors(fluent = true)
public class VoiceJoin {
    @JsonProperty("type")
    private final String type;
    @JsonProperty("guild_id")
    private final String guildId;
    @JsonProperty("session_id")
    private final String sessionId;
    @JsonProperty("endpoint")
    private final String endpoint;
    @JsonProperty("token")
    private final String token;
}
