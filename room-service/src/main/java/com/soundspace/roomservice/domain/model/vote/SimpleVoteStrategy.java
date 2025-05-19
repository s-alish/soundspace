package com.soundspace.roomservice.domain.model.vote;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class SimpleVoteStrategy implements VoteStrategy {

    @Override
    public Long selectNextTrack(List<Vote> votes) {
        if (votes.isEmpty()) {
            return null;
        }

        Map<Long, Long> voteCount = votes.stream()
                .collect(Collectors.groupingBy(Vote::getTrackId, Collectors.counting()));

        return voteCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(null);
    }
}