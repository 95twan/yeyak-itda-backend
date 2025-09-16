package com.rodemtree.yeyakitda.dto;

import lombok.Builder;

@Builder
public record MenuDto(
        Long id,
        String name,
        String description,
        Integer price,
        String imageUrl
) {
}
