package com.rodemtree.yeyakitda.dto;

import lombok.Builder;

@Builder
public record EventBannerDto(
        Long eventId,
        String title,
        String bannerImageUrl
) {
}
