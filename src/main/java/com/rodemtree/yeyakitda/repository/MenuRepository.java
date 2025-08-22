package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> {
    List<MenuEntity> findByRestaurant_Id(Long restaurantId);
}
