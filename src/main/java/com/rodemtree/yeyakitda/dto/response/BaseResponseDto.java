package com.rodemtree.yeyakitda.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.rodemtree.yeyakitda.dto.PageInfoDto;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record BaseResponseDto(
        int status,
        String message,
        Object data,
        PageInfoDto pageInfo
) {
    public static BaseResponseDto of(int status, String message) {
        return BaseResponseDto.of(status, message, null);
    }

    public static BaseResponseDto of(int status, String message, Object data) {
        return BaseResponseDto.of(status, message, data, null);
    }

    public static BaseResponseDto of(int status, String message, Object data, PageInfoDto pageInfo) {
        return new BaseResponseDto(status, message, data, pageInfo);
    }
}
