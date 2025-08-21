package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.request.SignUpRequestDto;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> signUp(@Valid @RequestBody SignUpRequestDto dto) {
        userService.signUp(dto);

        ApiResponseDto<?> responseDto = ApiResponseDto.of(HttpStatus.CREATED.value(), "성공적으로 회원가입 되었습니다.");

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
