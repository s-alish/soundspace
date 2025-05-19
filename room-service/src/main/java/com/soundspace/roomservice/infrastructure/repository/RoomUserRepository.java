package com.soundspace.roomservice.infrastructure.repository;

import com.soundspace.roomservice.domain.model.room.RoomUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomUserRepository extends JpaRepository<RoomUser, Long> {
    List<RoomUser> findByRoomId(Long roomId);
    boolean existsByRoomIdAndUserId(Long roomId, Long userId);
}