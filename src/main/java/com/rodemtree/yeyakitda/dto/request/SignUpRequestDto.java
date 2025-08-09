package com.rodemtree.yeyakitda.dto.request;

public record SignUpRequestDto(
    String name,
    String email,
    String password,
    String nickname,
    String address,
    String phoneNumber
) {
    public static SignUpRequestDto of(String name, String email, String password, String nickname, String address, String phoneNumber) {
        return new SignUpRequestDto(name, email, password, nickname, address, phoneNumber);
    }
}
