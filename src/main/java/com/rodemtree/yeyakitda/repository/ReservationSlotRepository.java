package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.ReservationSlotEntity;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReservationSlotRepository extends JpaRepository<ReservationSlotEntity, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT rs FROM ReservationSlotEntity rs WHERE rs.id = :id")
    Optional<ReservationSlotEntity> findByIdWithPessimisticLock(@Param("id") Long id);

    List<ReservationSlotEntity> findByRestaurant_IdAndSlotAtGreaterThanEqualAndSlotAtLessThan(Long restaurantId, LocalDateTime startDateTime, LocalDateTime endDateTime);
}
