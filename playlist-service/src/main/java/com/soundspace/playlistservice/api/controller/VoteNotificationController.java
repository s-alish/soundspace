package com.soundspace.playlistservice.api.controller;

import com.soundspace.playlistservice.api.dto.VoteProposalMessage;
import com.soundspace.playlistservice.api.dto.VoteResultMessage;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@RestController
@RequestMapping("/rooms/{roomId}")
public class VoteNotificationController {

    private final Map<Long, Map<String, SseEmitter>> roomEmitters = new ConcurrentHashMap<>();

    @GetMapping(value = "/vote-notifications", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeToVoteNotifications(@PathVariable Long roomId) {
        SseEmitter emitter = new SseEmitter(Long.MAX_VALUE); // No timeout
        roomEmitters.computeIfAbsent(roomId, k -> new ConcurrentHashMap<>())
                .put(emitter.toString(), emitter);

        emitter.onCompletion(() -> removeEmitter(roomId, emitter));
        emitter.onTimeout(() -> removeEmitter(roomId, emitter));
        emitter.onError((e) -> removeEmitter(roomId, emitter));

        // Send initial ping to keep connection alive
        try {
            emitter.send(SseEmitter.event().name("ping").data("Connected"));
        } catch (Exception e) {
            removeEmitter(roomId, emitter);
        }

        return emitter;
    }

    public void sendVoteProposal(Long roomId, VoteProposalMessage message) {
        sendEvent(roomId, "vote-proposal", message);
    }

    public void sendVoteResult(Long roomId, VoteResultMessage message) {
        sendEvent(roomId, "vote-result", message);
    }

    private void sendEvent(Long roomId, String eventName, Object data) {
        Map<String, SseEmitter> emitters = roomEmitters.get(roomId);
        if (emitters != null) {
            emitters.forEach((key, emitter) -> {
                try {
                    emitter.send(SseEmitter.event().name(eventName).data(data));
                } catch (Exception e) {
                    removeEmitter(roomId, emitter);
                }
            });
        }
    }

    private void removeEmitter(Long roomId, SseEmitter emitter) {
        Map<String, SseEmitter> emitters = roomEmitters.get(roomId);
        if (emitters != null) {
            emitters.remove(emitter.toString());
            if (emitters.isEmpty()) {
                roomEmitters.remove(roomId);
            }
        }
    }
}