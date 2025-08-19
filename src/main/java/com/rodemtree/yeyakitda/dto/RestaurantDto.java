package com.rodemtree.yeyakitda.dto;

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
