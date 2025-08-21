package com.rodemtree.yeyakitda.dto;

public record LoginSuccessDto(
        String accessToken,
        String refreshToken
) {
    public static LoginSuccessDto of(String accessToken, String refreshToken) {
        return new LoginSuccessDto(accessToken, refreshToken);
    }
}
