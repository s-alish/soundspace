package com.soundspace.roomservice.domain.model.playback;

public class PlayingState implements PlaybackStateInterface {

    private final Long currentTrackId;

    public PlayingState(Long trackId) {
        this.currentTrackId = trackId;
    }

    @Override
    public void play(PlaybackStateContext context, Long trackId) {
        context.setState(new PlayingState(trackId));
    }

    @Override
    public void pause(PlaybackStateContext context) {
        context.setState(new PausedState());
    }

    @Override
    public PlaybackState.PlaybackStateEnum getState() {
        return PlaybackState.PlaybackStateEnum.PLAYING;
    }

    @Override
    public Long getCurrentTrackId() {
        return currentTrackId;
    }
}
