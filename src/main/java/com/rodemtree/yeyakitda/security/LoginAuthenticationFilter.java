package com.rodemtree.yeyakitda.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodemtree.yeyakitda.dto.JwtUserInfoDto;
import com.rodemtree.yeyakitda.dto.request.LoginRequestDto;
import com.rodemtree.yeyakitda.dto.response.BaseResponseDto;
import com.rodemtree.yeyakitda.dto.response.LoginSuccessResponseDto;
import com.rodemtree.yeyakitda.entity.RefreshTokenEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.entity.UserRole;
import com.rodemtree.yeyakitda.repository.RefreshTokenRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import com.rodemtree.yeyakitda.util.JwtUtil;
import jakarta.persistence.EntityNotFoundException;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public LoginAuthenticationFilter(JwtUtil jwtUtil, ObjectMapper objectMapper, UserRepository userRepository, RefreshTokenRepository refreshTokenRepository) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.userRepository = userRepository;
        this.refreshTokenRepository = refreshTokenRepository;
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

        UserEntity userEntity = userRepository.findByEmail(userDetails.getUsername()).orElseThrow(() -> new EntityNotFoundException("인증된 사용자를 찾을 수 없습니다."));
        refreshTokenRepository.findByUser_Id(userEntity.getId()).ifPresentOrElse(savedToken -> savedToken.updateRefreshToken(refreshToken, expireAt),
                () -> {
                    RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder()
                            .token(refreshToken)
                            .user(userEntity)
                            .expireAt(expireAt)
                            .build();
                    refreshTokenRepository.save(refreshTokenEntity);
                }
        );

        response.setStatus(HttpStatus.OK.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        LoginSuccessResponseDto loginSuccessResponseDto = LoginSuccessResponseDto.of(accessToken, refreshToken);
        BaseResponseDto responseDto = BaseResponseDto.of(HttpStatus.OK.value(), "로그인 성공", loginSuccessResponseDto);
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        BaseResponseDto responseDto = BaseResponseDto.of(HttpStatus.UNAUTHORIZED.value(), "이메일 또는 비밀번호가 일치하지 않습니다.");
        response.getWriter().write(objectMapper.writeValueAsString(responseDto));
    }
}
