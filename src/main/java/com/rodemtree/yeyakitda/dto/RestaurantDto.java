package com.rodemtree.yeyakitda.dto;

import lombok.Builder;

@Builder
public record RestaurantDto(
        Long id,
        String name,
        String thumbnailImageUrl,
        String description,
        String address,
        String category,
        Float rating
) {

}
