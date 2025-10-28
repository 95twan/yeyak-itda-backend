package com.rodemtree.yeyakitda.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodemtree.yeyakitda.dto.JwtUserInfoDto;
import com.rodemtree.yeyakitda.dto.request.LoginRequestDto;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.LoginSuccessDto;
import com.rodemtree.yeyakitda.dto.response.ResponseErrorCode;
import com.rodemtree.yeyakitda.dto.response.ResponseSuccessCode;
import com.rodemtree.yeyakitda.entity.UserRole;
import com.rodemtree.yeyakitda.service.AuthService;
import com.rodemtree.yeyakitda.common.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.io.IOException;
import java.time.LocalDateTime;


public class LoginAuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final AuthService authService;

    public LoginAuthenticationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper, AuthService authService) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.authService = authService;
        setFilterProcessesUrl("/api/auth/login");
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        try {
            LoginRequestDto loginRequestDto = objectMapper.readValue(request.getInputStream(), LoginRequestDto.class);
            String email = loginRequestDto.email();
            String password = loginRequestDto.password();

            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(email, password);

            return getAuthenticationManager().authenticate(authenticationToken);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws java.io.IOException, ServletException {
        CustomUserDetails userDetails = (CustomUserDetails) authResult.getPrincipal();

        String role = userDetails.getAuthorities().stream()
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("User has no authorities"))
                .getAuthority();

        JwtUserInfoDto userInfoDto = JwtUserInfoDto.of(userDetails.getUsername(), UserRole.valueOf(role));

        String accessToken = jwtUtil.createAccessToken(userInfoDto);
        String refreshToken = jwtUtil.createRefreshToken(userInfoDto);
        LocalDateTime expireAt = jwtUtil.getExpiration(refreshToken);

        authService.saveOrUpdateRefreshToken(userDetails.getUsername(), refreshToken, expireAt);

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        LoginSuccessDto loginSuccessDto = LoginSuccessDto.of(accessToken, refreshToken);
        ApiResponseDto<?> responseDto = ApiResponseDto.of(ResponseSuccessCode.LOGIN, loginSuccessDto);
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponseDto<?> responseDto = ApiResponseDto.of(ResponseErrorCode.INVALID_EMAIL_PASSWORD);
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }
}
