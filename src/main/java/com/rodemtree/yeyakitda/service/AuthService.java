package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.JwtUserInfoDto;
import com.rodemtree.yeyakitda.dto.LoginSuccessDto;
import com.rodemtree.yeyakitda.entity.RefreshTokenEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.exception.InvalidTokenException;
import com.rodemtree.yeyakitda.repository.RefreshTokenRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import com.rodemtree.yeyakitda.util.JwtUtil;
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
    private final JwtUtil jwtUtil;

    @Transactional
    public void saveOrUpdateRefreshToken(String email, String refreshToken, LocalDateTime expireAt) {
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
        refreshTokenRepository.deleteByUserEmail(email);
    }


    @Transactional
    public LoginSuccessDto reissueToken(String refreshToken) {
        if (!jwtUtil.isTokenValid(refreshToken) || !"refresh".equals(jwtUtil.getType(refreshToken))) throw new InvalidTokenException();
        RefreshTokenEntity refreshTokenEntity = refreshTokenRepository.findByTokenWithUser(refreshToken).orElseThrow(InvalidTokenException::new);

        UserEntity userEntity = refreshTokenEntity.getUser();
        JwtUserInfoDto userInfoDto = JwtUserInfoDto.of(userEntity.getEmail(), userEntity.getRole());

        String newAccessToken = jwtUtil.createAccessToken(userInfoDto);
        String newRefreshToken = jwtUtil.createRefreshToken(userInfoDto);
        LocalDateTime newExpireAt = jwtUtil.getExpiration(newRefreshToken);

        refreshTokenEntity.updateRefreshToken(newRefreshToken, newExpireAt);

        return LoginSuccessDto.of(newAccessToken, newRefreshToken);
    }
}
