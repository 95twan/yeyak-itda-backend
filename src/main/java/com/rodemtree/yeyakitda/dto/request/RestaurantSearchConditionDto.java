package com.rodemtree.yeyakitda.dto.request;

import lombok.Builder;

import java.util.Set;

@Builder
public record RestaurantSearchConditionDto(
        Set<String> categories,
        String keyword,
        String theme
) {
}
