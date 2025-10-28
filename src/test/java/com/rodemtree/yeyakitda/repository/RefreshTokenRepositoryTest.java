package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.config.AbstractMySQLContainer;
import com.rodemtree.yeyakitda.config.JpaConfig;
import com.rodemtree.yeyakitda.entity.RefreshTokenEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@DisplayName("리포지토리 - RefreshToken")
class RefreshTokenRepositoryTest extends AbstractMySQLContainer {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;


    @Test
    @DisplayName("성공 - token으로 refreshToken과 관계된 유저를 함께 조회")
    void findByTokenWithUserTest() {
        // Given
        UserEntity userEntity = userRepository.save(createUser());
        String token = "refresh-token";
        refreshTokenRepository.save(createRefreshToken(userEntity, token));

        // When & Then
        Optional<RefreshTokenEntity> result = refreshTokenRepository.findByTokenWithUser(token);
        assertThat(result).isPresent();
        assertThat(result.get().getUser().getEmail()).isEqualTo(userEntity.getEmail());
    }

    @Test
    @DisplayName("실패 - 존재하지 않은 token으로 refreshToken을 조회")
    void findByNotExistTokenWithUserTest() {
        // Given
        String notExisttoken = "refresh-token";

        // When & Then
        assertThat(refreshTokenRepository.findByTokenWithUser(notExisttoken)).isEmpty();
    }

    @Test
    @DisplayName("성공 - user email로 refreshToken을 조회")
    void findByUserEmailTest() {
        // Given
        UserEntity userEntity = userRepository.save(createUser());
        refreshTokenRepository.save(createRefreshToken(userEntity, "refresh-token"));

        // When & Then
        assertThat(refreshTokenRepository.findByUser_Email(userEntity.getEmail())).isNotEmpty();
    }

    @Test
    @DisplayName("실패 - 없는 user email로 refreshToken을 조회")
    void findWithNotExistUserIdTest() {
        // Given
        String notExistEmail = "not@mail.com";

        // When & Then
        assertThat(refreshTokenRepository.findByUser_Email(notExistEmail)).isEmpty();
    }

    @Test
    @DisplayName("실패 - user email로 없는 refreshToken을 조회")
    void findWithUserIdNotExistTest() {
        // Given
        UserEntity userEntity = userRepository.save(createUser());

        // When & Then
        assertThat(refreshTokenRepository.findByUser_Email(userEntity.getEmail())).isEmpty();
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
