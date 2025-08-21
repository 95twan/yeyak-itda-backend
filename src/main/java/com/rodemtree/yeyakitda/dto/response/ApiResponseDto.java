package com.rodemtree.yeyakitda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponseDto<T>(
        int status,
        String message,
        T data
) {
    public static ApiResponseDto<?> of(int status, String message) {
        return ApiResponseDto.of(status, message, null);
    }

    public static <T> ApiResponseDto<T> of(int status, String message, T data) {
        return new ApiResponseDto<> (status, message, data);
    }
}
