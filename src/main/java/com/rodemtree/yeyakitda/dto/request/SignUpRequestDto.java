package com.rodemtree.yeyakitda.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record SignUpRequestDto(
        @NotBlank
        String name,
        @NotBlank
        @Email
        String email,
        @NotBlank
        String password,
        @NotBlank
        String nickname,
        @NotBlank
        String address,
        @NotBlank
        String phoneNumber
) {
    public static SignUpRequestDto of(String name, String email, String password, String nickname, String address, String phoneNumber) {
        return new SignUpRequestDto(name, email, password, nickname, address, phoneNumber);
    }
}
