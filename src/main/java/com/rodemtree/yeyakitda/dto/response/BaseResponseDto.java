package com.rodemtree.yeyakitda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseResponseDto(
        int status,
        String message,
        Object data
) {
    public static BaseResponseDto of(int status, String message) {
        return BaseResponseDto.of(status, message, null);
    }

    public static BaseResponseDto of(int status, String message, Object data) {
        return new BaseResponseDto(status, message, data);
    }
}
