package com.rodemtree.yeyakitda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponseDto<T>(
        int status,
        String message,
        T data
) {
    public static ApiResponseDto<?> of(ResponseSuccessCode code) {
        return new ApiResponseDto<> (code.getStatus(), code.getMessage(), null);
    }

    public static ApiResponseDto<?> of(ResponseErrorCode code) {
        return new ApiResponseDto<> (code.getStatus(), code.getMessage(), null);
    }

    public static <T> ApiResponseDto<T> of(ResponseSuccessCode code, T data) {
        return new ApiResponseDto<> (code.getStatus(), code.getMessage(), data);
    }
}
