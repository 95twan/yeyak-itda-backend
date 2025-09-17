package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    @Query("SELECT r FROM ReservationEntity r " +
            "JOIN FETCH r.restaurant " +
            "JOIN FETCH r.reservationSlot " +
            "WHERE r.user.email = :email")
    List<ReservationEntity> findByUser_EmailWithDetail(@Param("email") String email);
}
