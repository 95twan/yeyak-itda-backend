package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.ReviewDto;
import com.rodemtree.yeyakitda.mapper.ReviewMapper;
import com.rodemtree.yeyakitda.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewMapper reviewMapper;

    public List<ReviewDto> findTop10LatestReviews(Long restaurantId) {
        return reviewRepository.findByRestaurant_IdOrderByCreatedAtDesc(restaurantId, Limit.of(10)).stream()
                .map(reviewMapper::reviewEntityToReviewDto)
                .toList();
    }
}
