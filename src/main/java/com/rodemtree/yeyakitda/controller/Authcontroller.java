package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.request.ReissueTokenRequestDto;
import com.rodemtree.yeyakitda.dto.response.BaseResponseDto;
import com.rodemtree.yeyakitda.dto.response.LoginSuccessResponseDto;
import com.rodemtree.yeyakitda.security.CustomUserDetails;
import com.rodemtree.yeyakitda.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @PostMapping("/reissue")
    public ResponseEntity<BaseResponseDto> reissueToken(@RequestBody ReissueTokenRequestDto reissueTokenRequestDto) {
        LoginSuccessResponseDto loginSuccessResponseDto = authService.reissueToken(reissueTokenRequestDto.refreshToken());
        BaseResponseDto responseDto = BaseResponseDto.of(HttpStatus.OK.value(), "AccessToken이 성공적으로 발급되었습니다.", loginSuccessResponseDto);
        return ResponseEntity.ok(responseDto);
    }
}
