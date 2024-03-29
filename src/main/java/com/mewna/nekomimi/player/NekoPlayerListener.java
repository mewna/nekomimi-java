package com.mewna.nekomimi.player;

import com.mewna.nekomimi.Nekomimi;
import com.mewna.nekomimi.track.NekoTrack;
import com.mewna.nekomimi.track.NekoTrackEvent;
import com.mewna.nekomimi.track.NekoTrackEvent.TrackEventType;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import gg.amy.singyeong.client.query.QueryBuilder;
import io.vertx.core.json.JsonObject;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.experimental.Accessors;
import space.npstr.magma.api.MagmaMember;

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
        nekomimi.singyeong().send(new QueryBuilder().target("backend").build(),
                new JsonObject()
                        .put("type", TrackEventType.AUDIO_TRACK_START.name())
                        .put("data", new NekoTrackEvent(TrackEventType.AUDIO_TRACK_START, this.track).toJson()));
        nekomimi.queue(this.track.context().guild()).currentAudioTrack(track);
    }
    
    @Override
    public void onTrackEnd(final AudioPlayer player, final AudioTrack track, final AudioTrackEndReason endReason) {
        nekomimi.magma().removeSendHandler(MagmaMember.builder()
                .guildId(this.track.context().guild())
                .userId(System.getenv("CLIENT_ID"))
                .build());
        nekomimi.queue(this.track.context().guild()).currentAudioTrack(null);
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
