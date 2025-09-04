package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.ReviewDto;
import com.rodemtree.yeyakitda.dto.request.ReviewRequestDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.ReviewEntity;
import com.rodemtree.yeyakitda.entity.ReviewImageEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.mapper.ReviewMapper;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import com.rodemtree.yeyakitda.repository.ReviewImageRepository;
import com.rodemtree.yeyakitda.repository.ReviewRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Limit;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewImageRepository reviewImageRepository;
    private final UserRepository userRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReviewMapper reviewMapper;

    public List<ReviewDto> findTop10LatestReviews(Long restaurantId) {
        List<ReviewEntity> reviewEntities = reviewRepository.findByRestaurant_IdOrderByCreatedAtDesc(restaurantId, Limit.of(10));

        return reviewEntities.stream()
                .map(review -> {
                    List<ReviewImageEntity> images = reviewImageRepository.findByReview_Id(review.getId());
                    return reviewMapper.reviewEntityToReviewDto(review, images);
                }).toList();
    }

    @Transactional
    public void createReview(Long restaurantId, String userEmail, ReviewRequestDto reviewRequestDto, List<MultipartFile> images) {
        UserEntity userEntity = userRepository.findByEmail(userEmail).orElseThrow(() -> new EntityNotFoundException("사용자를 찾을 수 없습니다."));
        RestaurantEntity restaurantEntity = restaurantRepository.findById(restaurantId).orElseThrow(() -> new EntityNotFoundException("식당을 찾을 수 없습니다."));

        ReviewEntity reviewEntity = ReviewEntity.of(restaurantEntity, userEntity, reviewRequestDto.comment(), reviewRequestDto.rating());

        reviewRepository.save(reviewEntity);

        // Todo - 이미지 처리
    }
}
