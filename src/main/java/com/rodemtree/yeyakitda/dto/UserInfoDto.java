package com.rodemtree.yeyakitda.dto;

public record UserInfoDto(
        String name,
        String email,
        String nickname,
        String phoneNumber,
        String address
) {
    public static UserInfoDto of(String name, String email, String nickname, String phoneNumber, String address) {
        return new UserInfoDto(name, email, nickname, phoneNumber, address);
    }
}
