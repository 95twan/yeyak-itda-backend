package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.ReviewEntity;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
    List<ReviewEntity> findByRestaurant_IdOrderByCreatedAtDesc(Long restaurantId, Limit limit);
}
