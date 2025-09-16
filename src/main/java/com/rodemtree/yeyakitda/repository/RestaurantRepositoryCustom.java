package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.ThemeEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface RestaurantRepositoryCustom {
    Page<RestaurantEntity> search(RestaurantSearchConditionDto condition, Pageable pageable);
    List<RestaurantEntity> findTop10ByTheme(ThemeEntity theme);
    Page<RestaurantEntity> findByTheme(String themeTitle, Pageable pageable);
}
