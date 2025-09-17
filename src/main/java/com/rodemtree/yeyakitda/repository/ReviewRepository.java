package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.ReviewEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    @Query("SELECT r FROM ReviewEntity r JOIN FETCH r.user WHERE r.id = :restaurantId ORDER BY r.createdAt desc")
    List<ReviewEntity> findByRestaurant_IdOrderByCreatedAtDesc(@Param("restaurantId") Long restaurantId, Limit limit);
}
