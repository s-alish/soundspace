package com.soundspace.roomservice.api.controller;

import com.soundspace.roomservice.domain.model.playback.PlaybackState;
import com.soundspace.roomservice.domain.model.vote.Vote;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @MessageMapping("/rooms/{id}/state")
    @SendTo("/topic/rooms/{id}/state")
    public PlaybackState updateState(@DestinationVariable Long id, PlaybackState state) {
        return state; // Реальная логика в RoomService
    }

    @MessageMapping("/rooms/{id}/votes")
    @SendTo("/topic/rooms/{id}/votes")
    public Vote updateVote(@DestinationVariable Long id, Vote vote) {
        return vote; // Реальная логика в RoomService
    }
}