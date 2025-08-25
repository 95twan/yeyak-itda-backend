package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.*;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.entity.MenuEntity;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.RestaurantImageEntity;
import com.rodemtree.yeyakitda.mapper.MenuMapper;
import com.rodemtree.yeyakitda.mapper.RestaurantMapper;
import com.rodemtree.yeyakitda.repository.MenuRepository;
import com.rodemtree.yeyakitda.repository.RestaurantImageRepository;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final MenuRepository menuRepository;
    private final ReservationSlotService reservationSlotService;
    private final ReviewService reviewService;
    private final RestaurantMapper restaurantMapper;
    private final MenuMapper menuMapper;

    public Page<RestaurantDto> searchRestaurants(RestaurantSearchConditionDto condition, Pageable pageable) {
        Page<RestaurantEntity> restaurantEntities = restaurantRepository.search(condition, pageable);
        return restaurantEntities.map(restaurantMapper::restaurantEntityToRestaurantDto);
    }

    public RestaurantDetailDto getRestaurantDetail(Long restaurantId, LocalDate date) {
        RestaurantEntity restaurantEntity = restaurantRepository.findById(restaurantId).orElseThrow(EntityNotFoundException::new);
        List<RestaurantImageEntity> restaurantImageEntities = restaurantImageRepository.findByRestaurant_Id(restaurantId);
        RestaurantInfoDto restaurantInfoDto = restaurantMapper.restaurantEntityToRestaurantInfoDto(restaurantEntity, restaurantImageEntities);

        List<ReservationSlotDto> reservationSlotDtos = reservationSlotService.findReservationSlotsByDate(restaurantId, date);

        List<MenuEntity> menuEntities = menuRepository.findByRestaurant_Id(restaurantId);
        List<MenuDto> menuDtos = menuMapper.menuEntitiesToMenuDtos(menuEntities);

        List<ReviewDto> reviewDtos = reviewService.getTop10LatestReviews(restaurantId);

        return new RestaurantDetailDto(restaurantInfoDto, reservationSlotDtos, menuDtos, reviewDtos);
    }
}
