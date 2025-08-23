package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.RestaurantDetailDto;
import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.response.PagedResponseDto;
import com.rodemtree.yeyakitda.dto.response.ResponseSuccessCode;
import com.rodemtree.yeyakitda.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurantController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<PagedResponseDto<RestaurantDto>>> searchRestaurants(
            @ModelAttribute RestaurantSearchConditionDto condition,
            @PageableDefault(size = 12, page = 0, sort = {"rating"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RestaurantDto> restaurants = restaurantService.searchRestaurants(condition, pageable);
        PagedResponseDto<RestaurantDto> pagedResponseDto = PagedResponseDto.of(restaurants);
        ApiResponseDto<PagedResponseDto<RestaurantDto>> responseDto = ApiResponseDto.of(ResponseSuccessCode.RESTAURANTS, pagedResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @GetMapping("/{restaurantId}")
    public ResponseEntity<ApiResponseDto<RestaurantDetailDto>> getRestaurantDetail(@PathVariable Long restaurantId) {
        RestaurantDetailDto restaurantDetailDto = restaurantService.getRestaurantDetail(restaurantId);
        ApiResponseDto<RestaurantDetailDto> responseDto = ApiResponseDto.of(ResponseSuccessCode.RESTAURANT, restaurantDetailDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
