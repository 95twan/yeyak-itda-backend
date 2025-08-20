package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.mapper.RestuarantMapper;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestuarantMapper restuarantMapper;

    public Page<RestaurantDto> getRestaurantList(RestaurantSearchConditionDto condition, Pageable pageable) {
        Page<RestaurantEntity> restaurantEntityPage = restaurantRepository.search(condition, pageable);
        return restaurantEntityPage.map(restuarantMapper::restaurantEntityToRestaurantDto);
    }
}
