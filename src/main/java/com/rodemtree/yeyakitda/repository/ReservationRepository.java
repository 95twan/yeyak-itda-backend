package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.ReservationEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReservationRepository extends JpaRepository<ReservationEntity, Long> {
}
