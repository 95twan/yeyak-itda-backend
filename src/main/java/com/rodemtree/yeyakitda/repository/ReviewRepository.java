package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.ReviewEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<ReviewEntity, Long> {
}
