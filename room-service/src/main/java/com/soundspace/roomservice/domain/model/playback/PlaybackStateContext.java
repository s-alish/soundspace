package com.soundspace.roomservice.domain.model.playback;

public class PlaybackStateContext {

    private PlaybackStateInterface state;

    public PlaybackStateContext() {
        this.state = new PausedState();
    }

    public void setState(PlaybackStateInterface state) {
        this.state = state;
    }

    public void play(Long trackId) {
        state.play(this, trackId);
    }

    public void pause() {
        state.pause(this);
    }

    public PlaybackState.PlaybackStateEnum getCurrentState() {
        return state.getState();
    }

    public Long getCurrentTrackId() {
        return state.getCurrentTrackId();
    }
}