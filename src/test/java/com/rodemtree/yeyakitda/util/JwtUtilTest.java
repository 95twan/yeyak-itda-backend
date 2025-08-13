package com.rodemtree.yeyakitda.util;

import com.rodemtree.yeyakitda.dto.JwtUserInfoDto;
import com.rodemtree.yeyakitda.entity.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("유틸리티 - JWT")
class JwtUtilTest {
    private static final String SECRET = "my-super-secret-key-for-jwt-should-be-long-enough";
    private static final long ACCESS_TOKEN_EXPIRATION = 1000L * 60 * 60; // 1시간
    private static final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7;

    private JwtUtil jwtUtil;
    
    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(SECRET, ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
    }

    @Test
    @DisplayName("성공 - 사용자 이메일과 역할을 담은 Access Token을 생성한다.")
    void createAccessTokenTest() {
        // Given
        JwtUserInfoDto userInfoDto = createJwtUserInfoDto("test@test.com", UserRole.ROLE_USER);

        // When
        String accessToken = jwtUtil.createAccessToken(userInfoDto);

        // Then
        assertThat(accessToken).isNotNull();

    }

    @Test
    @DisplayName("성공 - 유효한 토큰에서 사용자 이메일을 추출한다.")
    void getEmailFromTokenTest() {
        // Given
        JwtUserInfoDto userInfoDto = createJwtUserInfoDto("test@test.com", UserRole.ROLE_USER);
        String token = jwtUtil.createAccessToken(userInfoDto);

        // When
        String extractedEmail = jwtUtil.getEmail(token);

        // Then
        assertThat(extractedEmail).isEqualTo(userInfoDto.email());

    }

    @Test
    @DisplayName("성공 - 유효한 토큰에서 사용자 Role을 추출한다.")
    void getRoleFromTokenTest() {
        // Given
        JwtUserInfoDto userInfoDto = createJwtUserInfoDto("test@test.com", UserRole.ROLE_USER);
        String token = jwtUtil.createAccessToken(userInfoDto);

        // When
        String extractedRole = jwtUtil.getRole(token);

        // Then
        assertThat(extractedRole).isEqualTo(userInfoDto.role().name());

    }

    @Test
    @DisplayName("성공 - 유효하지 않은 토큰은 false를 리턴한다.")
    void inValidTokenTest() {
        // Given
        jwtUtil = new JwtUtil(SECRET, -ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION);
        JwtUserInfoDto userInfoDto = createJwtUserInfoDto("test@test.com", UserRole.ROLE_USER);
        String token = jwtUtil.createAccessToken(userInfoDto);

        // When & Then
        assertThat(jwtUtil.isTokenValid(token)).isFalse();

    }

    private JwtUserInfoDto createJwtUserInfoDto(String email, UserRole role) {
        return JwtUserInfoDto.of(email, role);
    }
}
