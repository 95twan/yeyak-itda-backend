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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Limit;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@DisplayName("비즈니스 로직 - 리뷰")
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewImageRepository reviewImageRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    private final ReviewMapper reviewMapper = Mappers.getMapper(ReviewMapper.class);

    @BeforeEach
    public void setUp() {
        reviewService = new ReviewService(reviewRepository, reviewImageRepository, userRepository, restaurantRepository, reviewMapper);
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

    @Test
    @DisplayName("성공 - 최신 리뷰 10개 조회 시, 각 리뷰에 포함된 이미지 URL 목록을 함께 반환한다.")
    void findTop10LatestReviewsWithImagesTest() {
        // Given
        Long restaurantId = 1L;
        Limit limit = Limit.of(10);
        ReviewEntity reviewEntity1 = createReviewEntity(restaurantId, "이미지 있는 리뷰1", 5);
        ReviewEntity reviewEntity2 = createReviewEntity(2L, "이미지 없는 리뷰2", 4);
        List<ReviewEntity> reviewEntities = List.of(reviewEntity1, reviewEntity2);
        given(reviewRepository.findByRestaurant_IdOrderByCreatedAtDesc(restaurantId, limit)).willReturn(reviewEntities);

        ReviewImageEntity reviewImage1 = createReviewImage(reviewEntity1, "url1");
        ReviewImageEntity reviewImage2 = createReviewImage(reviewEntity1, "url2");
        List<ReviewImageEntity> images = List.of(reviewImage1, reviewImage2);
        given(reviewImageRepository.findByReview_Id(reviewEntity1.getId())).willReturn(images);
        given(reviewImageRepository.findByReview_Id(reviewEntity2.getId())).willReturn(List.of());


        // When
        List<ReviewDto> result = reviewService.findTop10LatestReviews(restaurantId);

        // Then
        assertThat(result).hasSize(2);

        assertThat(result.get(0).imageUrls()).hasSize(2);
        assertThat(result.get(0).imageUrls()).containsExactly("url1", "url2");

        assertThat(result.get(1).imageUrls()).isEmpty();

        then(reviewRepository).should().findByRestaurant_IdOrderByCreatedAtDesc(restaurantId, Limit.of(10));
        then(reviewImageRepository).should().findByReview_Id(reviewEntity1.getId());
        then(reviewImageRepository).should().findByReview_Id(reviewEntity2.getId());
    }

    @Test
    @DisplayName("성공 - 리뷰 정보가 주어지면, 리뷰를 생성하고 저장한다.")
    void createReviewTest() {
        // Given
        Long restaurantId = 1L;
        String userEmail = "test@test.com";
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(4, "test comment");
        List<MultipartFile> images = List.of();

        UserEntity userEntity = UserEntity.builder().email(userEmail).build();
        RestaurantEntity restaurantEntity = RestaurantEntity.builder().build();

        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(userEntity));
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurantEntity));

        // When
        reviewService.createReview(restaurantId, userEmail, reviewRequestDto, images);

        // Then
        ArgumentCaptor<ReviewEntity> reviewCaptor = ArgumentCaptor.forClass(ReviewEntity.class);
        then(reviewRepository).should().save(reviewCaptor.capture());
        ReviewEntity reviewEntity = reviewCaptor.getValue();

        assertThat(reviewEntity.getRating()).isEqualTo(reviewRequestDto.rating());
        assertThat(reviewEntity.getComment()).isEqualTo(reviewRequestDto.comment());
        assertThat(reviewEntity.getUser()).isEqualTo(userEntity);
        assertThat(reviewEntity.getRestaurant()).isEqualTo(restaurantEntity);

    }

    private ReviewEntity createReviewEntity(String comment, Integer rating) {
        return createReviewEntity(null, comment, rating);
    }

    private ReviewEntity createReviewEntity(Long id, String comment, Integer rating) {
        ReviewEntity review = ReviewEntity.of(null, null, comment, rating);
        ReflectionTestUtils.setField(review, "id", id);
        return review;
    }

    private ReviewImageEntity createReviewImage(ReviewEntity review, String imageUrl) {
        return ReviewImageEntity.of(review, imageUrl);
    }

}
