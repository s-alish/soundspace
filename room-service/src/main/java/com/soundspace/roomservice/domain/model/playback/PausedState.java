package com.soundspace.roomservice.domain.model.playback;

public class PausedState implements PlaybackStateInterface {

    @Override
    public void play(PlaybackStateContext context, Long trackId) {
        context.setState(new PlayingState(trackId));
    }

    @Override
    public void pause(PlaybackStateContext context) {
    }

    @Override
    public PlaybackState.PlaybackStateEnum getState() {
        return PlaybackState.PlaybackStateEnum.PAUSED;
    }

    @Override
    public Long getCurrentTrackId() {
        return null;
    }
}
