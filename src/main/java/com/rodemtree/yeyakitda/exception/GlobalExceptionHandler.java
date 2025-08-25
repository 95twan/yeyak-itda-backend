package com.rodemtree.yeyakitda.exception;

import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.response.ResponseErrorCode;
import jakarta.persistence.EntityNotFoundException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.nio.file.AccessDeniedException;
import java.util.stream.Collectors;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateException.class)
    public ResponseEntity<ApiResponseDto<?>> handleDuplicateException(DuplicateException e) {
        ApiResponseDto<?> responseDto = ApiResponseDto.of(HttpStatus.CONFLICT.value(), e.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(responseDto);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponseDto<?>> handleMethodArgumentNotValidException(MethodArgumentNotValidException e) {
        String errorMessage = e.getBindingResult().getFieldErrors().stream()
                .map(fieldError -> fieldError.getField() + " : " + fieldError.getDefaultMessage())
                .collect(Collectors.joining(", "));
        ApiResponseDto<?> responseDto = ApiResponseDto.of(HttpStatus.BAD_REQUEST.value(), errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }

    @ExceptionHandler(InvalidTokenException.class)
    public ResponseEntity<ApiResponseDto<?>> handleInvalidTokenException(InvalidTokenException e) {
        ApiResponseDto<?> responseDto = ApiResponseDto.of(ResponseErrorCode.INVALID_TOKEN);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(responseDto);
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiResponseDto<?>> handleEntityNotFoundException(EntityNotFoundException e) {
        ApiResponseDto<?> responseDto = ApiResponseDto.of(ResponseErrorCode.RESOURCE_NOT_FOUND);
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(responseDto);
    }

    @ExceptionHandler(PropertyReferenceException.class)
    public ResponseEntity<ApiResponseDto<?>> handlePropertyReferenceException(PropertyReferenceException e) {
        String errorMessage = "잘못된 정렬 기준입니다: '" + e.getPropertyName() + "'";
        ApiResponseDto<?> responseDto = ApiResponseDto.of(HttpStatus.BAD_REQUEST.value(), errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiResponseDto<?>> handleAccessDeniedException(AccessDeniedException e) {
        ApiResponseDto<?> responseDto = ApiResponseDto.of(ResponseErrorCode.ACCESS_DENIED);
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(responseDto);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ApiResponseDto<?>> handleMissingServletRequestParameterException(MissingServletRequestParameterException e) {
        String errorMessage = e.getParameterName() + " : " + "필수 입력 파라미터입니다.";
        ApiResponseDto<?> responseDto = ApiResponseDto.of(HttpStatus.BAD_REQUEST.value(), errorMessage);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }

    @ExceptionHandler(InvalidRequestException.class)
    public ResponseEntity<ApiResponseDto<?>> handleInvalidRequestException(InvalidRequestException e) {
        ApiResponseDto<?> responseDto = ApiResponseDto.of(e.getResponseErrorCode());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(responseDto);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseDto<?>> handleException(Exception e) {
        ApiResponseDto<?> responseDto = ApiResponseDto.of(ResponseErrorCode.INTERNAL_SEVER_ERROR);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(responseDto);
    }

}
