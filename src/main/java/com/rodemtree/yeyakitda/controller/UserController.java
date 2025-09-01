package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.UserInfoDto;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.response.ResponseSuccessCode;
import com.rodemtree.yeyakitda.security.CustomUserDetails;
import com.rodemtree.yeyakitda.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponseDto<UserInfoDto>> getMyInfo(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        String userEmail = userDetails.getUsername();
        UserInfoDto userInfoDto = userService.getUserInfo(userEmail);
        ApiResponseDto<UserInfoDto> responseDto = ApiResponseDto.of(ResponseSuccessCode.USER_INFO, userInfoDto);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
