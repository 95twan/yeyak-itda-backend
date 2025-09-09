package com.rodemtree.yeyakitda.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record EventDto(
        Long id,
        String title,
        String content,
        String bannerImageUrl,
        LocalDateTime startDate,
        LocalDateTime endDate
) {
}
