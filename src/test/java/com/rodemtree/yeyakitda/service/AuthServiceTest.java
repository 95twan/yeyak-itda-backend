package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.response.LoginSuccessResponseDto;
import com.rodemtree.yeyakitda.entity.RefreshTokenEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.repository.RefreshTokenRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import com.rodemtree.yeyakitda.util.JwtUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("비즈니스 로직 - 인증")
class AuthServiceTest {
    
    @InjectMocks
    private AuthService authService;
    
    @Mock
    private UserRepository userRepository;
    
    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtUtil jwtUtil;

    @Test
    @DisplayName("성공 - 신규 로그인 시, Refresh Token을 새로 저장한다.")
    void newLoginSavesNewRefreshTokenTest() {
        // Given
        String email = "test@test.com";
        String refreshToken = "refreshToken";
        LocalDateTime expireAt = LocalDateTime.now();

        UserEntity userEntity = createUser(email);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(userEntity));

        given(refreshTokenRepository.findByUser_Email(userEntity.getEmail())).willReturn(Optional.empty());

        // When
        authService.updateRefreshToken(email, refreshToken, expireAt);

        // Then
        then(refreshTokenRepository).should().save(any());
    }

    @Test
    @DisplayName("성공 - 재로그인 시, 기존 Refresh Token을 갱신한다.")
    void newLoginUpdateExistRefreshTokenTest() {
        // Given
        String email = "test@test.com";
        String newRefreshToken = "newRefreshToken";
        LocalDateTime newExpireAt = LocalDateTime.now().plusDays(7);

        UserEntity userEntity = createUser(email);
        given(userRepository.findByEmail(email)).willReturn(Optional.of(userEntity));

        RefreshTokenEntity refreshTokenEntity = mock(RefreshTokenEntity.class);
        given(refreshTokenRepository.findByUser_Email(userEntity.getEmail())).willReturn(Optional.of(refreshTokenEntity));

        // When
        authService.updateRefreshToken(email, newRefreshToken, newExpireAt);

        // Then
        then(refreshTokenEntity).should().updateRefreshToken(newRefreshToken, newExpireAt);
    }

    @Test
    @DisplayName("성공 - 사용자 이메일을 받으면, 해당 사용자의 Refresh Token을 삭제한다.")
    void deleteRefreshTokenTest() {
        // Given
        String email = "test@test.com";
        RefreshTokenEntity refreshTokenEntity = mock(RefreshTokenEntity.class);
        given(refreshTokenRepository.findByUser_Email(email)).willReturn(Optional.of(refreshTokenEntity));

        // When
        authService.deleteRefreshToken(email);

        // Then
        then(refreshTokenRepository).should().delete(refreshTokenEntity);

    }

    @Test
    @DisplayName("성공 - 유효한 refreshToken을 받으면, 새로운 accessToken과 refreshToken을 발급한다.")
    void reissueTokenWithValidTokenTest() {
        // Given
        String refreshToken = "refreshToken";

        given(jwtUtil.isTokenValid(refreshToken)).willReturn(true);
        given(jwtUtil.getType(refreshToken)).willReturn("refresh");

        RefreshTokenEntity refreshTokenEntity = mock(RefreshTokenEntity.class);
        given(refreshTokenRepository.findByToken(refreshToken)).willReturn(Optional.of(refreshTokenEntity));

        UserEntity userEntity = mock(UserEntity.class);
        given(refreshTokenEntity.getUser()).willReturn(userEntity);

        String newAccessToken = "newAccessToken";
        String newRefreshToken = "newRefreshToken";
        LocalDateTime newExpireAt = LocalDateTime.now().plusDays(7);
        given(jwtUtil.createAccessToken(any())).willReturn(newAccessToken);
        given(jwtUtil.createRefreshToken(any())).willReturn(newRefreshToken);
        given(jwtUtil.getExpiration(any())).willReturn(newExpireAt);

        // When
        LoginSuccessResponseDto loginSuccessResponseDto = authService.reissueToken(refreshToken);

        // Then
        assertThat(loginSuccessResponseDto.accessToken()).isEqualTo(newAccessToken);
        assertThat(loginSuccessResponseDto.refreshToken()).isEqualTo(newRefreshToken);

        then(refreshTokenEntity).should().updateRefreshToken(eq(newRefreshToken), any(LocalDateTime.class));
    }


    private UserEntity createUser(String email) {
        UserEntity userEntity = UserEntity.builder()
                .name("홍길동")
                .address("경기도 구리시")
                .email(email)
                .password("encoded-pw")
                .phoneNumber("010-1234-1234")
                .nickname("test")
                .build();
        userEntity.setDefaultRole();
        return userEntity;
    }
}
