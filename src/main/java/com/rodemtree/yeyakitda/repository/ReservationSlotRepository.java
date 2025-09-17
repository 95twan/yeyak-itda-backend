package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.ReservationSlotEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlotEntity, Long> {
    List<ReservationSlotEntity> findByRestaurant_IdAndSlotAtGreaterThanEqualAndSlotAtLessThan(Long restaurantId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
