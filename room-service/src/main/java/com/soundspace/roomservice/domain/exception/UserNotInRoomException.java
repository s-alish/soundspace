package com.soundspace.roomservice.domain.exception;

public class UserNotInRoomException extends RuntimeException {
    public UserNotInRoomException(Long userId, Long roomId) {
        super("User " + userId + " is not in room " + roomId);
    }
}