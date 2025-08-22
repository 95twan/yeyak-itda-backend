package com.rodemtree.yeyakitda.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record RestaurantInfoDto(
        Long id,
        Long userId,
        String name,
        List<String> imageUrls,
        String description,
        List<String> tags,
        String address,
        String category,
        String phoneNumber,
        Float rating,
        List<OperatingHourDto> operatingHours
) {
}
