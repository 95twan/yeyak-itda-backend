package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface RestaurantRepositoryCustom {
    Page<RestaurantEntity> search(RestaurantSearchConditionDto condition, Pageable pageable);
}
