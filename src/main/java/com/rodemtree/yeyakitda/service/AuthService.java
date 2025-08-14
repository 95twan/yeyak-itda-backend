package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.entity.RefreshTokenEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.repository.RefreshTokenRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public void updateRefreshToken(String email, String refreshToken, LocalDateTime expireAt) {
        UserEntity userEntity = userRepository.findByEmail(email).orElseThrow(() -> new EntityNotFoundException("인증된 사용자를 찾을 수 없습니다."));
        refreshTokenRepository.findByUser_Email(email).ifPresentOrElse(
                savedToken -> savedToken.updateRefreshToken(refreshToken, expireAt),
                () -> refreshTokenRepository.save(RefreshTokenEntity.builder()
                        .token(refreshToken)
                        .user(userEntity)
                        .expireAt(expireAt)
                        .build())

        );
    }

    @Transactional
    public void deleteRefreshToken(String email) {
        refreshTokenRepository.findByUser_Email(email).ifPresent(refreshTokenRepository::delete);
    }
}
