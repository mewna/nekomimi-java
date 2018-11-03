package com.mewna.nekomimi;

import com.mewna.nekomimi.NekoTrackEvent.TrackEventType;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import gg.amy.singyeong.QueryBuilder;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import space.npstr.magma.MagmaMember;

/**
 * @author amy
 * @since 10/31/18.
 */
@Getter
@Accessors(fluent = true)
@RequiredArgsConstructor
public class NekoPlayerListener extends AudioEventAdapter {
    private final Nekomimi nekomimi;
    private final NekoTrack track;
    
    @Override
    public void onTrackStart(final AudioPlayer player, final AudioTrack track) {
        nekomimi.singyeong().send("mewna-backend", new QueryBuilder().build(),
                new JsonObject()
                        .put("type", TrackEventType.AUDIO_TRACK_START.name())
                        .put("data", new NekoTrackEvent(TrackEventType.AUDIO_TRACK_START, this.track).toJson()));
    }
    
    @Override
    public void onTrackEnd(final AudioPlayer player, final AudioTrack track, final AudioTrackEndReason endReason) {
        nekomimi.magma().removeSendHandler(MagmaMember.builder()
                .guildId(this.track.context().guild())
                .userId(System.getenv("CLIENT_ID"))
                .build());
        nekomimi.playNextInQueue(this.track.context().guild());
    }
    
    @Override
    public void onTrackException(final AudioPlayer player, final AudioTrack track, final FriendlyException exception) {
        // TODO: Emit exception and move on
        exception.printStackTrace();
    }
    
    @Override
    public void onTrackStuck(final AudioPlayer player, final AudioTrack track, final long thresholdMs) {
        // TODO: Emit stuck and move on
    }
}
