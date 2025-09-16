package com.rodemtree.yeyakitda.dto;

import lombok.Builder;

@Builder
public record UserInfoDto(
        String name,
        String email,
        String nickname,
        String phoneNumber,
        String address
) {
}
