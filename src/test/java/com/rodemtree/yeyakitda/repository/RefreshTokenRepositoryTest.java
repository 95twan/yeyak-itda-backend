package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.config.JpaConfig;
import com.rodemtree.yeyakitda.entity.RefreshTokenEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("리포지토리 - Refresh Token")
class RefreshTokenRepositoryTest {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;


    @Test
    @DisplayName("성공 - User와 RefreshToken으로 RefreshToken을 저장한다.")
    void saveTest() {
        // Given
        UserEntity userEntity = userRepository.save(createUser());
        String refreshToken = "refresh-token";
        RefreshTokenEntity refreshTokenEntity = createRefreshToken(userEntity, refreshToken);

        // When
        RefreshTokenEntity savedRefreshToken = refreshTokenRepository.save(refreshTokenEntity);

        // Then
        assertThat(savedRefreshToken.getId()).isNotNull();
        assertThat(savedRefreshToken.getToken()).isEqualTo(refreshToken);

    }

    @Test
    @DisplayName("성공 - userId로 존재하는 refreshToken을 조회")
    void findWithUserIdTest() {
        // Given
        UserEntity userEntity = userRepository.save(createUser());
        refreshTokenRepository.save(createRefreshToken(userEntity, "refresh-token"));

        // When & Then
        assertThat(refreshTokenRepository.findByUser_Id(userEntity.getId())).isNotEmpty();
    }

    @Test
    @DisplayName("실패 - 없는 userId로 refreshToken을 조회")
    void findWithNotExistUserIdTest() {
        // Given
        Long notExistUserId = 999L;

        // When & Then
        assertThat(refreshTokenRepository.findByUser_Id(notExistUserId)).isEmpty();
    }

    @Test
    @DisplayName("실패 - userId로 없는 refreshToken을 조회")
    void findWithUserIdNotExistTest() {
        // Given
        UserEntity userEntity = userRepository.save(createUser());

        // When & Then
        assertThat(refreshTokenRepository.findByUser_Id(userEntity.getId())).isEmpty();
    }

    @Test
    @DisplayName("성공 - token으로 refreshToken을 조회")
    void findWithTokenTest() {
        // Given
        UserEntity userEntity = userRepository.save(createUser());
        String token = "refresh-token";
        refreshTokenRepository.save(createRefreshToken(userEntity, token));

        // When & Then
        assertThat(refreshTokenRepository.findByToken(token)).isNotEmpty();
    }

    @Test
    @DisplayName("실패 - 존재하지 않은 token으로 refreshToken을 조회")
    void findWithNotExistTokenTest() {
        // Given
        String notExisttoken = "refresh-token";

        // When & Then
        assertThat(refreshTokenRepository.findByToken(notExisttoken)).isEmpty();
    }

    private UserEntity createUser() {
        UserEntity userEntity = UserEntity.builder()
                .name("홍길동")
                .address("경기도 구리시")
                .email("test@test.com")
                .password("encoded-pw")
                .phoneNumber("010-1234-1234")
                .nickname("test")
                .build();
        userEntity.setDefaultRole();
        return userEntity;
    }

    private RefreshTokenEntity createRefreshToken(UserEntity user, String token) {
        return RefreshTokenEntity.builder()
                .user(user)
                .token(token)
                .expireAt(LocalDateTime.now().plusDays(1))
                .build();
    }
}
