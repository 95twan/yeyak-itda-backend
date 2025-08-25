package com.rodemtree.yeyakitda.exception;

import com.rodemtree.yeyakitda.dto.response.ResponseErrorCode;
import lombok.Getter;

@Getter
public class InvalidRequestException extends RuntimeException {

    private final ResponseErrorCode responseErrorCode;

    public InvalidRequestException(ResponseErrorCode responseErrorCode) {
        super(responseErrorCode.getMessage());
        this.responseErrorCode = responseErrorCode;
    }
}
