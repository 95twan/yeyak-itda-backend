package com.rodemtree.yeyakitda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseResponseDto(
        int status,
        String message,
        Object data
) {
    public static BaseResponseDto of(int status, String message) {
        return new BaseResponseDto(status, message, null);
    }
}
