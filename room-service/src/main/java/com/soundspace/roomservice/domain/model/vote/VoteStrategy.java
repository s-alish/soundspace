package com.soundspace.roomservice.domain.model.vote;

import java.util.List;

public interface VoteStrategy {
    Long selectNextTrack(List<Vote> votes);
}