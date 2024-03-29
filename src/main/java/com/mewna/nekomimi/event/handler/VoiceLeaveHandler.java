package com.mewna.nekomimi.event.handler;

import com.mewna.nekomimi.Nekomimi;
import com.mewna.nekomimi.event.VoiceLeave;
import gg.amy.singyeong.client.SingyeongType;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.npstr.magma.api.MagmaMember;
import space.npstr.magma.api.Member;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @author amy
 * @since 6/9/19.
 */
@RequiredArgsConstructor
public class VoiceLeaveHandler implements Consumer<VoiceLeave> {
    private final Nekomimi nekomimi;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void accept(final VoiceLeave voiceLeave) {
        final Member member = MagmaMember.builder()
                .guildId(voiceLeave.guildId())
                .userId(Nekomimi.USER_ID)
                .build();
        logger.info("Got voice leave for {}", member);
        nekomimi.magma().removeSendHandler(member);
        nekomimi.magma().closeConnection(member);
        nekomimi.guilds().remove(voiceLeave.guildId());
        nekomimi.statsClient().gauge("activeVcs", nekomimi.guilds().size());
        nekomimi.singyeong().updateMetadata("guilds", SingyeongType.LIST,
                new JsonArray(new ArrayList<>(nekomimi.guilds())));
    }
}
