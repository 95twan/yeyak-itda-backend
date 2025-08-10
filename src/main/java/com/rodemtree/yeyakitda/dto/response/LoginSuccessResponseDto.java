package com.rodemtree.yeyakitda.dto.response;

public record LoginSuccessResponseDto(
        String accessToken,
        String refreshToken
) {
    public static LoginSuccessResponseDto of(String accessToken, String refreshToken) {
        return new LoginSuccessResponseDto(accessToken, refreshToken);
    }
}
