package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.RestaurantImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RestaurantImageRepository extends JpaRepository<RestaurantImageEntity, Long> {
    List<RestaurantImageEntity> findByRestaurant_Id(Long restaurantId);
}
