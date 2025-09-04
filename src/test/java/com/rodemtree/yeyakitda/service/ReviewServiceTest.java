package com.rodemtree.yeyakitda.service;


import com.rodemtree.yeyakitda.dto.ReviewDto;
import com.rodemtree.yeyakitda.dto.request.ReviewRequestDto;
import com.rodemtree.yeyakitda.entity.*;
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
import org.springframework.mock.web.MockMultipartFile;
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
    private ImageService imageService;

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
        reviewService = new ReviewService(imageService, reviewRepository, reviewImageRepository, userRepository, restaurantRepository, reviewMapper);
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

        UserEntity userEntity = UserEntity.builder().email(userEmail).build();
        RestaurantEntity restaurantEntity = RestaurantEntity.builder().build();

        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(userEntity));
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurantEntity));

        // When
        reviewService.createReview(restaurantId, userEmail, reviewRequestDto, null);

        // Then
        ArgumentCaptor<ReviewEntity> reviewCaptor = ArgumentCaptor.forClass(ReviewEntity.class);
        then(reviewRepository).should().save(reviewCaptor.capture());
        ReviewEntity reviewEntity = reviewCaptor.getValue();

        assertThat(reviewEntity.getRating()).isEqualTo(reviewRequestDto.rating());
        assertThat(reviewEntity.getComment()).isEqualTo(reviewRequestDto.comment());
        assertThat(reviewEntity.getUser()).isEqualTo(userEntity);
        assertThat(reviewEntity.getRestaurant()).isEqualTo(restaurantEntity);

    }

    @Test
    @DisplayName("성공 - 리뷰 정보와 이미지 파일들이 주어지면, 이미지 업로드 후 리뷰와 이미지 정보를 함께 저장한다.")
    void createReviewWithImagesTest() {
        // Given
        Long restaurantId = 1L;
        String userEmail = "test@test.com";
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(5, "정말 맛있는 곳입니다!");

        MockMultipartFile image1 = new MockMultipartFile("images", "image1.jpg", "image/jpeg", "image1 content".getBytes());
        MockMultipartFile image2 = new MockMultipartFile("images", "image2.png", "image/png", "image2 content".getBytes());
        List<MultipartFile> images = List.of(image1, image2);

        UserEntity userEntity = UserEntity.builder().email(userEmail).build();
        RestaurantEntity restaurantEntity = RestaurantEntity.builder().build();

        ReviewEntity reviewEntity = ReviewEntity.of(restaurantEntity, userEntity, reviewRequestDto.comment(), reviewRequestDto.rating());
        ReflectionTestUtils.setField(reviewEntity, "id", 1L); // given(reviewRepository.save(...))가 반환할 객체

        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(userEntity));
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurantEntity));
        given(reviewRepository.save(any(ReviewEntity.class))).willReturn(reviewEntity);

        given(imageService.upload(image1, ImageDomain.REVIEW)).willReturn("reviews/path-to-image1.jpg");
        given(imageService.upload(image2, ImageDomain.REVIEW)).willReturn("reviews/path-to-image2.png");

        // When
        reviewService.createReview(restaurantId, userEmail, reviewRequestDto, images);

        // Then
        then(reviewRepository).should().save(any(ReviewEntity.class));

        then(imageService).should().upload(image1, ImageDomain.REVIEW);
        then(imageService).should().upload(image2, ImageDomain.REVIEW);

        ArgumentCaptor<List<ReviewImageEntity>> reviewImageEntitiesCaptor = ArgumentCaptor.forClass(List.class);
        then(reviewImageRepository).should().saveAll(reviewImageEntitiesCaptor.capture());
        List<ReviewImageEntity> savedImages = reviewImageEntitiesCaptor.getValue();

        assertThat(savedImages).hasSize(2);
        assertThat(savedImages.get(0).getImageUrl()).isEqualTo("reviews/path-to-image1.jpg");
        assertThat(savedImages.get(0).getReview()).isEqualTo(reviewEntity);
        assertThat(savedImages.get(1).getImageUrl()).isEqualTo("reviews/path-to-image2.png");
        assertThat(savedImages.get(1).getReview()).isEqualTo(reviewEntity);
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
