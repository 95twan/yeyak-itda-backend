package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import org.springframework.data.jpa.repository.JpaRepository;


public interface RestaurantRepository extends JpaRepository<RestaurantEntity, Long>, RestaurantRepositoryCustom {

}
