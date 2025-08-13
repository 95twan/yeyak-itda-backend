package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.entity.RefreshTokenEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.repository.RefreshTokenRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
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

    @Test
    @DisplayName("성공 - 신규 로그인 시, Refresh Token을 새로 저장한다.")
    void newLoginSavesNewRefreshToken() {
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
    void newLoginUpdateExistRefreshToken() {
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
