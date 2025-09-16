package com.rodemtree.yeyakitda.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RestaurantGroupDto(
        Long id,
        String title,
        List<RestaurantDto> restaurants
) {

}
