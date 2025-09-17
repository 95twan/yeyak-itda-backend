package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.entity.ReviewImageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewImageRepository extends JpaRepository<ReviewImageEntity, Long> {
    List<ReviewImageEntity> findByReview_IdIn(List<Long> reviewIds);
}
