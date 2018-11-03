package com.mewna.nekomimi;

import com.google.common.collect.ImmutableList;
import com.mewna.nekomimi.NekoTrackEvent.TrackEventType;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import gg.amy.singyeong.QueryBuilder;
import io.vertx.core.json.JsonObject;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.Accessors;

/**
 * @author amy
 * @since 10/31/18.
 */
@Getter
@Accessors(fluent = true)
@AllArgsConstructor
public class NekoTrackLoader implements AudioLoadResultHandler {
    private final Nekomimi nekomimi;
    private final NekoTrackContext ctx;
    
    @Override
    public void trackLoaded(final AudioTrack track) {
        final AudioTrackInfo info = track.getInfo();
        final NekoTrack nt = new NekoTrack(info.uri, info.title, info.author, info.length, 0L, ctx);
        nekomimi.loadTracks(ctx.guild(), ImmutableList.of(nt));
        nekomimi.singyeong().send("mewna-backend", new QueryBuilder().build(),
                new JsonObject()
                .put("type", TrackEventType.AUDIO_TRACK_QUEUE.name())
                .put("data", new NekoTrackEvent(TrackEventType.AUDIO_TRACK_QUEUE, nt).toJson()));
    }
    
    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        // TODO
    }
    
    @Override
    public void noMatches() {
        // TODO
    }
    
    @Override
    public void loadFailed(final FriendlyException exception) {
        // TODO
    }
}
