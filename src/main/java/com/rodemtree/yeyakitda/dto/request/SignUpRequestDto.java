package com.rodemtree.yeyakitda.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SignUpRequestDto(
        @NotBlank(message = "이름은 필수 입력 항목입니다.")
        @Size(max = 32)
        String name,
        @NotBlank
        @Email
        @Size(max = 64)
        String email,
        @NotBlank
        @Size(max = 256)
        String password,
        @NotBlank
        @Size(max = 64)
        String nickname,
        @NotBlank
        @Size(max = 128)
        String address,
        @NotBlank
        @Size(max = 32)
        String phoneNumber
) {
    public static SignUpRequestDto of(String name, String email, String password, String nickname, String address, String phoneNumber) {
        return new SignUpRequestDto(name, email, password, nickname, address, phoneNumber);
    }
}
