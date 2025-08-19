package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface RestaurantRepositoryCustom {
    Page<RestaurantEntity> findByCategories(Set<String> categories, Pageable pageable);
}
