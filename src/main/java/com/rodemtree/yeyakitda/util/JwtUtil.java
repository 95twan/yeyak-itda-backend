package com.rodemtree.yeyakitda.util;

import com.rodemtree.yeyakitda.dto.JwtUserInfoDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final long accessTokenExpiration;
    private final long refreshTokenExpiration;

    public JwtUtil(@Value("${jwt.secret}") String secret, @Value("${jwt.expiration.access}") long accessTokenExpirationexpiration, @Value("${jwt.expiration.refresh}") long refreshTokenExpiration) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpirationexpiration;
        this.refreshTokenExpiration = refreshTokenExpiration;
    }

    public String createAccessToken(JwtUserInfoDto userInfoDto) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + accessTokenExpiration);
        return Jwts.builder()
                .subject(userInfoDto.email())
                .claim("role", userInfoDto.role().name())
                .claim("type", "access")
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    public String createRefreshToken(JwtUserInfoDto userInfoDto) {
        Date now = new Date();
        Date expirationDate = new Date(now.getTime() + refreshTokenExpiration);
        return Jwts.builder()
                .subject(userInfoDto.email())
                .claim("role", userInfoDto.role().name())
                .claim("type", "refresh")
                .issuedAt(now)
                .expiration(expirationDate)
                .signWith(secretKey)
                .compact();
    }

    public String getEmail(String token) {
        return getClaims(token).getSubject();
    }

    public String getRole(String token) {
        return getClaims(token).get("role", String.class);
    }

    // Todo - Enum 형식으로 고려
    public String getType(String token) {
        return getClaims(token).get("type", String.class);
    }

    public LocalDateTime getExpiration(String token) {
        return getClaims(token).getExpiration().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public boolean isAccessToken(String token) {
        try {
            return "access".equals(getType(token));
        } catch (Exception e) {
            return false;
        }

    }

    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
