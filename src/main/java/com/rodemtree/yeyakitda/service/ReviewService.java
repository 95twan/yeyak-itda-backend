package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.ReviewDto;
import com.rodemtree.yeyakitda.entity.ReviewEntity;
import com.rodemtree.yeyakitda.entity.ReviewImageEntity;
import com.rodemtree.yeyakitda.mapper.ReviewMapper;
import com.rodemtree.yeyakitda.repository.ReviewImageRepository;
import com.rodemtree.yeyakitda.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final ReviewMapper reviewMapper;

    public List<ReviewDto> findTop10LatestReviews(Long restaurantId) {
        List<ReviewEntity> reviewEntities = reviewRepository.findByRestaurant_IdOrderByCreatedAtDesc(restaurantId, Limit.of(10));

        return reviewEntities.stream()
                .map(review -> {
                    List<ReviewImageEntity> images = reviewImageRepository.findByReview_Id(review.getId());
                    return reviewMapper.reviewEntityToReviewDto(review, images);
                }).toList();
    }
}
