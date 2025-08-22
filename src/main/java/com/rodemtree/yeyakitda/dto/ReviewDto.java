package com.rodemtree.yeyakitda.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record ReviewDto(
        Long id,
        Long userId,
        String userNickname,
        List<String> imageUrls,
        String comment,
        Float rating
) {
}
