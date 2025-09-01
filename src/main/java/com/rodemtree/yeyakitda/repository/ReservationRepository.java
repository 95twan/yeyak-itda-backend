package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
    List<ReservationEntity> findByUser_Email(String email);
}
