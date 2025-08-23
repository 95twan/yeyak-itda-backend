package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.request.ReissueTokenRequestDto;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.LoginSuccessDto;
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
    public ResponseEntity<ApiResponseDto<?>> logout(@AuthenticationPrincipal CustomUserDetails customUserDetails) {
        String email = customUserDetails.getUsername();
        authService.deleteRefreshToken(email);
        ApiResponseDto<?> responseDto = ApiResponseDto.of(200, "성공적으로 로그아웃 되었습니다.");
        return ResponseEntity.ok(responseDto);
    }

    @PostMapping("/reissue")
    public ResponseEntity<ApiResponseDto<LoginSuccessDto>> reissueToken(@RequestBody ReissueTokenRequestDto reissueTokenRequestDto) {
        LoginSuccessDto loginSuccessDto = authService.reissueToken(reissueTokenRequestDto.refreshToken());
        ApiResponseDto<LoginSuccessDto> responseDto = ApiResponseDto.of(HttpStatus.OK.value(), "AccessToken이 성공적으로 발급되었습니다.", loginSuccessDto);
        return ResponseEntity.ok(responseDto);
    }
}
