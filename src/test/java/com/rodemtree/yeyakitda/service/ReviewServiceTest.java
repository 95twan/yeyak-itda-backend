package com.rodemtree.yeyakitda.service;


import com.rodemtree.yeyakitda.dto.ReviewDto;
import com.rodemtree.yeyakitda.entity.ReviewEntity;
import com.rodemtree.yeyakitda.mapper.ReviewMapper;
import com.rodemtree.yeyakitda.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("비즈니스 로직 - 리뷰")
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    private final ReviewMapper reviewMapper = Mappers.getMapper(ReviewMapper.class);

    @BeforeEach
    public void setUp() {
        reviewService = new ReviewService(reviewRepository, reviewMapper);
    }

    @Test
    @DisplayName("성공 - 식당 Id로 식당의 최신 리뷰 10개를 반환한다.")
    void findTop10LatestReviewsTest() {
        // Given
        Long restaurantId = 1L;
        Limit limit = Limit.of(10);
        ReviewEntity reviewEntity1 = createReviewEntity("테스트 리뷰1", 5);
        ReviewEntity reviewEntity2 = createReviewEntity("테스트 리뷰2", 4);
        List<ReviewEntity> reviewEntities = List.of(reviewEntity1, reviewEntity2);
        given(reviewRepository.findByRestaurant_IdOrderByCreatedAtDesc(restaurantId, limit)).willReturn(reviewEntities);

        // When
        List<ReviewDto> result = reviewService.findTop10LatestReviews(restaurantId);

        // Then
        then(reviewRepository).should().findByRestaurant_IdOrderByCreatedAtDesc(restaurantId, limit);
        assertThat(result.size()).isEqualTo(reviewEntities.size());
        assertThat(result.get(0).comment()).isEqualTo(reviewEntities.get(0).getComment());

    }

    private ReviewEntity createReviewEntity(String comment, Integer rating) {
        return ReviewEntity.of(null, null, comment, rating);
    }
}
