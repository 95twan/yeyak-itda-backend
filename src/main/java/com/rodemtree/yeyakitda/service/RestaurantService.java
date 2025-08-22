package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.*;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.entity.MenuEntity;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.RestaurantImageEntity;
import com.rodemtree.yeyakitda.entity.ReviewEntity;
import com.rodemtree.yeyakitda.mapper.MenuMapper;
import com.rodemtree.yeyakitda.repository.MenuRepository;
import com.rodemtree.yeyakitda.mapper.RestuarantMapper;
import com.rodemtree.yeyakitda.repository.RestaurantImageRepository;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final MenuRepository menuRepository;
    private final ReviewService reviewService;
    private final RestuarantMapper restuarantMapper;
    private final MenuMapper menuMapper;

    public Page<RestaurantDto> getRestaurants(RestaurantSearchConditionDto condition, Pageable pageable) {
        Page<RestaurantEntity> restaurantEntityPage = restaurantRepository.search(condition, pageable);
        return restaurantEntityPage.map(restuarantMapper::restaurantEntityToRestaurantDto);
    }

    public RestaurantDetailDto getRestaurant(Long restaurantId) {
        RestaurantEntity restaurantEntity = restaurantRepository.findById(restaurantId).orElseThrow(EntityNotFoundException::new);
        List<RestaurantImageEntity> restaurantImageEntitieList = restaurantImageRepository.findByRestaurant_Id(restaurantId);
        RestaurantInfoDto restaurantInfoDto = restuarantMapper.restaurantEntityToRestaurantInfoDto(restaurantEntity, restaurantImageEntitieList);

        List<MenuEntity> menuEntityList = menuRepository.findByRestaurant_Id(restaurantId);
        List<MenuDto> menuDtoList = menuMapper.menuEntityListToMenuDtoList(menuEntityList);

        List<ReviewDto> reviewDtoList = reviewService.getTop10LatestReviews(restaurantId);

        return new RestaurantDetailDto(restaurantInfoDto, menuDtoList, reviewDtoList);
    }
}
