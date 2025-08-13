package com.rodemtree.yeyakitda.entity;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@Entity
@Table(name = "refresh_token")
public class RefreshTokenEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @Column(name = "token", length = 256, nullable = false)
    private String token;

    @Column(name = "expire_at", nullable = false)
    private LocalDateTime expireAt;

    @Builder
    private RefreshTokenEntity(UserEntity user, String token, LocalDateTime expireAt) {
        this.user = user;
        this.token = token;
        this.expireAt = expireAt;
    }
}
