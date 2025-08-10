package com.rodemtree.yeyakitda.dto;

import com.rodemtree.yeyakitda.entity.UserRole;

public record JwtUserInfoDto (
    String email,
    UserRole role
) {
    public static JwtUserInfoDto of(String email, UserRole role) {
        return new JwtUserInfoDto(email, role);
    }
}
