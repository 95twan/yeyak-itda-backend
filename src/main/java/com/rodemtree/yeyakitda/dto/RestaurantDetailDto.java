package com.rodemtree.yeyakitda.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RestaurantDetailDto(
        RestaurantInfoDto restaurant,
        List<MenuDto> menus,
        List<ReviewDto> reviews
) {
}
