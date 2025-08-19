package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Set;

public interface RestaurantRepositoryCustom {
    Page<RestaurantEntity> findByCategoriesAndKeyword(Set<String> categories, String Keyword, Pageable pageable);
}
