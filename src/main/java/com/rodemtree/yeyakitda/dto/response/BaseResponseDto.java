package com.rodemtree.yeyakitda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseResponseDto(
        int code,
        String message,
        Object data
) {
    public static BaseResponseDto of(int code, String message) {
        return new BaseResponseDto(code, message, null);
    }
}
