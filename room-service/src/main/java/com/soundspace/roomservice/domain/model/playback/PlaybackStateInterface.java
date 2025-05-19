package com.soundspace.roomservice.domain.model.playback;

public interface PlaybackStateInterface {
    void play(PlaybackStateContext context, Long trackId);
    void pause(PlaybackStateContext context);
    PlaybackState.PlaybackStateEnum getState();
    Long getCurrentTrackId();
}
