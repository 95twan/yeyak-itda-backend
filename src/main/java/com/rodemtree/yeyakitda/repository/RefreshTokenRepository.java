package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.RefreshTokenEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface RefreshTokenRepository extends JpaRepository<RefreshTokenEntity, Long> {

    @Query("SELECT rt FROM RefreshTokenEntity rt JOIN FETCH rt.user WHERE rt.token = :token")
    Optional<RefreshTokenEntity> findByTokenWithUser(@Param("token") String token);

    Optional<RefreshTokenEntity> findByUser_Email(String email);

    @Modifying
    @Query("DELETE FROM RefreshTokenEntity rt WHERE rt.user.email = :email")
    void deleteByUserEmail(String email);
}
