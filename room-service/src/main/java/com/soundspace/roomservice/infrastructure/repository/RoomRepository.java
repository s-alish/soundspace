package com.soundspace.roomservice.infrastructure.repository;

import com.soundspace.roomservice.domain.model.room.Room;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {
}
