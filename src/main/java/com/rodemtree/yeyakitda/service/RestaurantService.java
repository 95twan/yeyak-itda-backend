package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.document.OperatingHour;
import com.rodemtree.yeyakitda.document.RestaurantOperatingHours;
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
import com.rodemtree.yeyakitda.repository.mongodb.RestaurantOperatingHoursRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RestaurantService {

    private final RestaurantRepository restaurantRepository;
    private final RestaurantImageRepository restaurantImageRepository;
    private final MenuRepository menuRepository;
    private final RestaurantOperatingHoursRepository restaurantOperatingHoursRepository;
    private final ReservationSlotService reservationSlotService;
    private final ReviewService reviewService;
    private final RestaurantMapper restaurantMapper;
    private final MenuMapper menuMapper;

    public Page<RestaurantDto> searchRestaurants(RestaurantSearchConditionDto condition, Pageable pageable) {
        Page<RestaurantEntity> restaurantEntities = restaurantRepository.search(condition, pageable);
        return restaurantEntities.map(restaurantMapper::restaurantEntityToRestaurantDto);
    }

    public Page<RestaurantDto> searchRestaurantsByTheme(String themeTitle, Pageable pageable) {
        Page<RestaurantEntity> restaurantEntities = restaurantRepository.findByTheme(themeTitle, pageable);
        return restaurantEntities.map(restaurantMapper::restaurantEntityToRestaurantDto);
    }

    public RestaurantDetailDto getRestaurantDetail(Long restaurantId, LocalDate date) {
        RestaurantEntity restaurantEntity = restaurantRepository.findById(restaurantId).orElseThrow(EntityNotFoundException::new);
        List<RestaurantImageEntity> restaurantImageEntities = restaurantImageRepository.findByRestaurant_Id(restaurantId);
        Optional<RestaurantOperatingHours> restaurantOperatingHours = restaurantOperatingHoursRepository.findByRestaurantId(restaurantId);
        List<OperatingHour> operatingHours = restaurantOperatingHours.map(RestaurantOperatingHours::getOperatingHours).orElse(List.of());
        RestaurantInfoDto restaurantInfoDto = restaurantMapper.restaurantEntityToRestaurantInfoDto(restaurantEntity, restaurantImageEntities, operatingHours);

        List<ReservationSlotDto> reservationSlotDtos = reservationSlotService.findReservationSlotsByDate(restaurantId, date);

        List<MenuEntity> menuEntities = menuRepository.findByRestaurant_Id(restaurantId);
        List<MenuDto> menuDtos = menuMapper.menuEntitiesToMenuDtos(menuEntities);

        List<ReviewDto> reviewDtos = reviewService.findTop10LatestReviews(restaurantId);

        return new RestaurantDetailDto(restaurantInfoDto, reservationSlotDtos, menuDtos, reviewDtos);
    }
}
