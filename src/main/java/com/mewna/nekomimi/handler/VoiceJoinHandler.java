package com.mewna.nekomimi.handler;

import com.mewna.nekomimi.Nekomimi;
import com.mewna.nekomimi.message.VoiceJoin;
import com.mewna.nekomimi.track.NekoTrackQueue;
import gg.amy.singyeong.client.SingyeongType;
import io.vertx.core.json.JsonArray;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import space.npstr.magma.api.MagmaMember;
import space.npstr.magma.api.MagmaServerUpdate;
import space.npstr.magma.api.Member;
import space.npstr.magma.api.ServerUpdate;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * @author amy
 * @since 6/9/19.
 */
@RequiredArgsConstructor
public class VoiceJoinHandler implements Consumer<VoiceJoin> {
    private final Nekomimi nekomimi;
    private final Logger logger = LoggerFactory.getLogger(getClass());
    
    @Override
    public void accept(final VoiceJoin voiceJoin) {
        final Member member = MagmaMember.builder()
                .guildId(voiceJoin.guildId())
                .userId(Nekomimi.USER_ID)
                .build();
        final ServerUpdate serverUpdate = MagmaServerUpdate.builder()
                .sessionId(voiceJoin.sessionId())
                .endpoint(voiceJoin.endpoint())
                .token(voiceJoin.token())
                .build();
        
        logger.info("Got voice join to {} {}", member, serverUpdate);
        nekomimi.magma().provideVoiceServerUpdate(member, serverUpdate);
        
        nekomimi.guilds().add(voiceJoin.guildId());
        nekomimi.singyeong().updateMetadata("guilds", SingyeongType.LIST,
                new JsonArray(new ArrayList<>(nekomimi.guilds())));
        
        nekomimi.statsClient().gauge("activeVcs", nekomimi.guilds().size());
        
        nekomimi.queues().putIfAbsent(voiceJoin.guildId(), new NekoTrackQueue(voiceJoin.guildId()));
    }
}
