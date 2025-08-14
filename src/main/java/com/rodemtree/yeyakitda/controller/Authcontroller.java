package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.response.BaseResponseDto;
import com.rodemtree.yeyakitda.security.CustomUserDetails;
import com.rodemtree.yeyakitda.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class Authcontroller {

    private final AuthService authService;

    @DeleteMapping("/logout")
    public ResponseEntity<BaseResponseDto> logout(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        authService.deleteRefreshToken(email);
        BaseResponseDto responseDto = BaseResponseDto.of(200, "성공적으로 로그아웃 되었습니다.");
        return ResponseEntity.ok(responseDto);
    }
}
