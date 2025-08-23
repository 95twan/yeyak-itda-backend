package com.rodemtree.yeyakitda.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ResponseErrorCode {
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "인증이 필요한 서비스입니다."),
    EMAIL_PASSWORD_WRONG(HttpStatus.UNAUTHORIZED.value(), "이메일 또는 비밀번호가 일치하지 않습니다."),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED.value(), "유효하지 않은 토큰입니다."),
    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND.value(), "요청한 리소스를 찾을 수 없습니다."),
    ACCESS_DENIED(HttpStatus.FORBIDDEN.value(), "권한이 없습니다."),
    INTERNAL_SEVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR.value(), "서버에 오류가 발생했습니다.");

    @Getter
    private final int status;
    @Getter
    private final String message;

    ResponseErrorCode(int status, String message) {
        this.status = status;
        this.message = message;
    }
}
