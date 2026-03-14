package com.omra.platform.repository;

import com.omra.platform.entity.Room;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RoomRepository extends JpaRepository<Room, Long> {

    Page<Room> findByHotelIdAndDeletedAtIsNull(Long hotelId, Pageable pageable);

    boolean existsByHotelIdAndRoomNumberAndDeletedAtIsNull(Long hotelId, String roomNumber);
}
