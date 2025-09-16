package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.RestaurantGroupDto;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.response.ResponseSuccessCode;
import com.rodemtree.yeyakitda.service.RestaurantGroupService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/restaurant-groups")
@RequiredArgsConstructor
public class RestaurantGroupController {

    private final RestaurantGroupService restaurantGroupService;

    @GetMapping(params = "group=theme")
    public ResponseEntity<ApiResponseDto<List<RestaurantGroupDto>>> getThemeGroupedRestaurants() {
        List<RestaurantGroupDto> restaurantGroupDtos = restaurantGroupService.findThemeGroupedRestaurants();
        ApiResponseDto<List<RestaurantGroupDto>> responseDto = ApiResponseDto.of(ResponseSuccessCode.RESTAURANT_THEME_GROUP, restaurantGroupDtos);
        return ResponseEntity.ok(responseDto);
    }
}
