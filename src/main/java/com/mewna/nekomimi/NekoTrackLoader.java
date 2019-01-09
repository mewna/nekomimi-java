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

import java.util.List;
import java.util.stream.Collectors;

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
    private final boolean singleTrack;
    
    @Override
    public void trackLoaded(final AudioTrack track) {
        final AudioTrackInfo info = track.getInfo();
        final NekoTrack nt = new NekoTrack(info.uri, info.title, info.author, info.length, 0L, ctx);
        nekomimi.loadTracks(ctx.guild(), ImmutableList.of(nt));
        nekomimi.statsClient().gauge("loadedTracks", nekomimi.queues().values().stream()
                .mapToLong(NekoTrackQueue::countTracks).sum());
        nekomimi.singyeong().send("mewna-backend", new QueryBuilder().build(),
                new JsonObject()
                .put("type", TrackEventType.AUDIO_TRACK_QUEUE.name())
                .put("data", new NekoTrackEvent(TrackEventType.AUDIO_TRACK_QUEUE, nt).toJson()));
    }
    
    @Override
    public void playlistLoaded(final AudioPlaylist playlist) {
        if(singleTrack) {
            final List<AudioTrack> tracks = playlist.getTracks();
            if(tracks.isEmpty()) {
                nekomimi.statsClient().gauge("loadedTracks", nekomimi.queues().values().stream()
                        .mapToLong(NekoTrackQueue::countTracks).sum());
                nekomimi.singyeong().send("mewna-backend", new QueryBuilder().build(),
                        new JsonObject()
                                .put("type", TrackEventType.AUDIO_TRACK_QUEUE.name())
                                .put("data", new NekoTrackEvent(TrackEventType.AUDIO_TRACK_NO_MATCHES, null).toJson()));
            } else {
                // Queue the first track
                final AudioTrack track = tracks.get(0);
                final AudioTrackInfo info = track.getInfo();
                final NekoTrack nt = new NekoTrack(info.uri, info.title, info.author, info.length, 0L, ctx);
                nekomimi.loadTracks(ctx.guild(), ImmutableList.of(nt));
                nekomimi.statsClient().gauge("loadedTracks", nekomimi.queues().values().stream()
                        .mapToLong(NekoTrackQueue::countTracks).sum());
                nekomimi.singyeong().send("mewna-backend", new QueryBuilder().build(),
                        new JsonObject()
                                .put("type", TrackEventType.AUDIO_TRACK_QUEUE.name())
                                .put("data", new NekoTrackEvent(TrackEventType.AUDIO_TRACK_QUEUE, nt).toJson()));
            }
        } else {
            final List<AudioTrack> tracks = playlist.getTracks();
            if(tracks.isEmpty()) {
                nekomimi.statsClient().gauge("loadedTracks", nekomimi.queues().values().stream()
                        .mapToLong(NekoTrackQueue::countTracks).sum());
                nekomimi.singyeong().send("mewna-backend", new QueryBuilder().build(),
                        new JsonObject()
                                .put("type", TrackEventType.AUDIO_TRACK_QUEUE.name())
                                .put("data", new NekoTrackEvent(TrackEventType.AUDIO_TRACK_NO_MATCHES, null).toJson()));
            } else {
                final List<NekoTrack> nts = tracks.stream()
                        .map(AudioTrack::getInfo)
                        .map(e -> new NekoTrack(e.uri, e.title, e.author, e.length, 0L, ctx))
                        .collect(Collectors.toList());
                nekomimi.loadTracks(ctx.guild(), ImmutableList.copyOf(nts));
                nekomimi.statsClient().gauge("loadedTracks", nekomimi.queues().values().stream()
                        .mapToLong(NekoTrackQueue::countTracks).sum());
                nekomimi.singyeong().send("mewna-backend", new QueryBuilder().build(),
                        new JsonObject()
                                .put("type", TrackEventType.AUDIO_TRACK_QUEUE_MANY.name())
                                .put("data", new NekoTrackEvent(TrackEventType.AUDIO_TRACK_QUEUE_MANY,
                                        new NekoTrack(null,"" + nts.size(),null,0L, 0L, ctx))
                                        .toJson()));
            }
        }
    }
    
    @Override
    public void noMatches() {
        nekomimi.singyeong().send("mewna-backend", new QueryBuilder().build(),
                new JsonObject()
                        .put("type", TrackEventType.AUDIO_TRACK_QUEUE.name())
                        .put("data", new NekoTrackEvent(TrackEventType.AUDIO_TRACK_NO_MATCHES, null).toJson()));
    }
    
    @Override
    public void loadFailed(final FriendlyException exception) {
        // TODO: Emit failure and capture
    }
}
