package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.document.OperatingHour;
import com.rodemtree.yeyakitda.document.RestaurantOperatingHours;
import com.rodemtree.yeyakitda.dto.ReservationSlotDto;
import com.rodemtree.yeyakitda.dto.RestaurantDetailDto;
import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.dto.ReviewDto;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.entity.MenuEntity;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.RestaurantImageEntity;
import com.rodemtree.yeyakitda.mapper.MenuMapper;
import com.rodemtree.yeyakitda.mapper.RestaurantMapper;
import com.rodemtree.yeyakitda.repository.MenuRepository;
import com.rodemtree.yeyakitda.repository.RestaurantImageRepository;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import com.rodemtree.yeyakitda.repository.mongodb.RestaurantOperatingHoursRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("비즈니스 로직 - 식당")
@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    private RestaurantService restaurantService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestaurantImageRepository restaurantImageRepository;

    @Mock
    private MenuRepository menuRepository;

    @Mock
    private RestaurantOperatingHoursRepository restaurantOperatingHoursRepository;

    @Mock
    private ReservationSlotService reservationSlotService;

    @Mock
    private ReviewService reviewService;

    private final RestaurantMapper restaurantMapper = Mappers.getMapper(RestaurantMapper.class);
    private final MenuMapper menuMapper = Mappers.getMapper(MenuMapper.class);

    @BeforeEach
    void setUp() {
        restaurantService = new RestaurantService(
                restaurantRepository,
                restaurantImageRepository,
                menuRepository,
                restaurantOperatingHoursRepository,
                reservationSlotService,
                reviewService,
                restaurantMapper,
                menuMapper
        );
    }

    @Test
    @DisplayName("성공 - 페이징 정보를 받아 식당 목록을 조회하면 식당 DTO 페이지를 반환한다.")
    void searchRestaurantsTest() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().build();

        List<RestaurantEntity> restaurantEntities = IntStream.range(1, 11)
                .mapToObj(i -> createRestaurant("restaurant" + i))
                .toList();
        Page<RestaurantEntity> restaurantEntityPage = new PageImpl<>(restaurantEntities, pageable, 10);
        given(restaurantRepository.search(condition, pageable)).willReturn(restaurantEntityPage);

        // When
        Page<RestaurantDto> result = restaurantService.searchRestaurants(condition, pageable);

        // Then
        then(restaurantRepository).should().search(condition, pageable);

        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getContent().get(0).name()).isEqualTo("restaurant1");

    }

    @Test
    @DisplayName("실패 - 유효하지 않은 필드로 정렬을 요청하면, PropertyReferenceException을 던진다.")
    void searchRestaurantsWithInvalidSortTest() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("invalid").descending());
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().build();

        given(restaurantRepository.search(condition, pageable))
                .willThrow(new PropertyReferenceException("invalid", TypeInformation.of(RestaurantEntity.class), Collections.emptyList()));

        // When & Then
        assertThatThrownBy(() -> restaurantService.searchRestaurants(condition, pageable))
                .isInstanceOf(PropertyReferenceException.class);

        then(restaurantRepository).should().search(condition, pageable);
    }

    @Test
    @DisplayName("성공 - 카테고리로 식당 목록을 조회하면 해당 카테고리의 식당 Dto 페이지를 반환한다.")
    void searchRestaurantsWithCategoryTest() {
        // Given
        Set<String> categories = Set.of("한식");
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().categories(categories).build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());

        Page<RestaurantEntity> restaurantEntityPage = new PageImpl<>(List.of(createRestaurant("테스트 식당", "한식,중식")), pageable, 1);
        given(restaurantRepository.search(condition, pageable)).willReturn(restaurantEntityPage);

        // When
        Page<RestaurantDto> result = restaurantService.searchRestaurants(condition, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).category()).contains(categories);

        then(restaurantRepository).should().search(condition, pageable);
    }

    @Test
    @DisplayName("성공 - 키워드로 식당 목록을 조회하면 이름, 상세내용, 주소에 키워드가 포함된 식당 Dto 페이지를 반환한다.")
    void searchRestaurantsWithKeywordTest() {
        // Given
        String keyword = "테스트";
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().keyword(keyword).build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());
        given(restaurantRepository.search(condition, pageable)).willReturn(Page.empty());

        // When
        Page<RestaurantDto> result = restaurantService.searchRestaurants(condition, pageable);

        // Then
        then(restaurantRepository).should().search(condition, pageable);
    }

    @Test
    @DisplayName("성공 - 카테고리와 키워드로 식당 목록을 조회하면 해당하는 식당 Dto 페이지를 반환한다.")
    void searchRestaurantsWithCategoriesAndKeywordTest() {
        // Given
        Set<String> categories = Set.of("한식");
        String keyword = "테스트";
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().categories(categories).keyword(keyword).build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());
        given(restaurantRepository.search(condition, pageable)).willReturn(Page.empty());

        // When
        restaurantService.searchRestaurants(condition, pageable);

        // Then
        then(restaurantRepository).should().search(condition, pageable);
    }

    @Test
    @DisplayName("성공 - 테마 이름으로 식당 목록을 조회하면 식당 DTO 페이지를 반환한다.")
    void searchRestaurantsByThemeTest() {
        // Given
        String theme = "인기 식당";
        Pageable pageable = PageRequest.of(0, 10);

        Page<RestaurantEntity> restaurantEntities = new PageImpl<>(List.of(createRestaurant("테스트 식당")), pageable, 1);

        given(restaurantRepository.findByTheme(theme, pageable)).willReturn(restaurantEntities);

        // When
        Page<RestaurantDto> result = restaurantService.searchRestaurantsByTheme(theme, pageable);

        // Then
        then(restaurantRepository).should().findByTheme(theme, pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().get(0).name()).isEqualTo("테스트 식당");

    }

    @Test
    @DisplayName("성공 - 식당 Id를 받아 식당을 조회하면 식당 DTO를 반환한다.")
    void getRestaurantDetailTest() {
        // Given
        Long restaurantId = 1L;
        RestaurantEntity restaurantEntity = createRestaurant("테스트 식당");
        ReflectionTestUtils.setField(restaurantEntity, "id", restaurantId);
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurantEntity));

        LocalDate date = LocalDate.now();

        // When
        RestaurantDetailDto result = restaurantService.getRestaurantDetail(restaurantId, date);

        // Then
        then(restaurantRepository).should().findById(restaurantId);

        assertThat(result.restaurant().id()).isEqualTo(restaurantEntity.getId());
        assertThat(result.restaurant().name()).isEqualTo(restaurantEntity.getName());
    }

    @Test
    @DisplayName("실패 - 없는 식당 Id를 받아 식당을 조회하면 EntityNotFoundException을 반환한다.")
    void getRestaurantDetailWithNotExistIdTest() {
        // Given
        Long restaurantId = 999L;
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.empty());

        LocalDate date = LocalDate.now();

        // When & Then
        assertThatThrownBy(() -> restaurantService.getRestaurantDetail(restaurantId, date))
                .isInstanceOf(EntityNotFoundException.class);

        then(restaurantRepository).should().findById(restaurantId);
    }

    @Test
    @DisplayName("성공 - 식당 Id를 받아 식당을 조회하면 식당 이미지를 포함한 식당 DTO를 반환한다.")
    void getRestaurantWithRestaurantDetailImageTest() {
        // Given
        Long restaurantId = 1L;
        RestaurantEntity restaurantEntity = createRestaurant("테스트 식당");
        ReflectionTestUtils.setField(restaurantEntity, "id", restaurantId);
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurantEntity));

        RestaurantImageEntity restaurantImage1 = RestaurantImageEntity.of(restaurantEntity, "https://test.com/image1.png");
        RestaurantImageEntity restaurantImage2 = RestaurantImageEntity.of(restaurantEntity, "https://test.com/image2.png");
        List<RestaurantImageEntity> restaurantImages = List.of(restaurantImage1, restaurantImage2);
        given(restaurantImageRepository.findByRestaurant_Id(restaurantId)).willReturn(restaurantImages);

        LocalDate date = LocalDate.now();

        // When
        RestaurantDetailDto result = restaurantService.getRestaurantDetail(restaurantId, date);

        // Then
        then(restaurantRepository).should().findById(restaurantId);
        then(restaurantImageRepository).should().findByRestaurant_Id(restaurantId);

        assertThat(result.restaurant().imageUrls()).hasSize(restaurantImages.size());
    }

    @Test
    @DisplayName("성공 - 식당 Id를 받아 식당을 조회하면 메뉴를 포함한 식당 DTO를 반환한다.")
    void getRestaurantDetailWithMenuTest() {
        // Given
        Long restaurantId = 1L;
        RestaurantEntity restaurantEntity = createRestaurant("테스트 식당");
        ReflectionTestUtils.setField(restaurantEntity, "id", restaurantId);
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurantEntity));

        MenuEntity menu1 = createMenu(restaurantEntity, "테스트 메뉴1");
        MenuEntity menu2 = createMenu(restaurantEntity, "테스트 메뉴2");
        List<MenuEntity> menus = List.of(menu1, menu2);
        given(menuRepository.findByRestaurant_Id(restaurantId)).willReturn(menus);

        LocalDate date = LocalDate.now();

        // When
        RestaurantDetailDto result = restaurantService.getRestaurantDetail(restaurantId, date);

        // Then
        then(restaurantRepository).should().findById(restaurantId);
        then(menuRepository).should().findByRestaurant_Id(restaurantId);

        assertThat(result.menus()).hasSize(menus.size());
    }

    @Test
    @DisplayName("성공 - 식당 Id를 받아 식당을 조회하면 최신 리뷰10개를 포함한 식당 DTO를 반환한다.")
    void getRestaurantDetailWithReviewTest() {
        // Given
        Long restaurantId = 1L;
        RestaurantEntity restaurantEntity = createRestaurant("테스트 식당");
        ReflectionTestUtils.setField(restaurantEntity, "id", restaurantId);
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurantEntity));

        ReviewDto review1 = createReveiewDto("테스트 리뷰1");
        ReviewDto review2 = createReveiewDto("테스트 리뷰2");
        List<ReviewDto> reviews = List.of(review1, review2);
        given(reviewService.findTop10LatestReviews(restaurantId)).willReturn(reviews);

        LocalDate date = LocalDate.now();

        // When
        RestaurantDetailDto result = restaurantService.getRestaurantDetail(restaurantId, date);

        // Then
        then(restaurantRepository).should().findById(restaurantId);
        then(reviewService).should().findTop10LatestReviews(restaurantId);

        assertThat(result.reviews()).hasSize(reviews.size());
    }

    @Test
    @DisplayName("성공 - 식당 Id를 받아 식당을 조회하면 당일 영업시간 내 예약 가능한 ReservationSlot 포함한 식당 DTO를 반환한다.")
    void getRestaurantDetailWithReservationSlotsTest() {
        // Given
        Long restaurantId = 1L;
        RestaurantEntity restaurantEntity = createRestaurant("테스트 식당");
        ReflectionTestUtils.setField(restaurantEntity, "id", restaurantId);
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurantEntity));

        LocalDateTime now = LocalDateTime.now();

        ReservationSlotDto reservationSlot1 = createReservationSlot(now.plusHours(1));
        ReservationSlotDto reservationSlot2 = createReservationSlot(now.plusHours(2));
        List<ReservationSlotDto> reservationSlots = List.of(reservationSlot1, reservationSlot2);
        given(reservationSlotService.findReservationSlotsByDate(restaurantId, now.toLocalDate())).willReturn(reservationSlots);

        // When
        RestaurantDetailDto result = restaurantService.getRestaurantDetail(restaurantId, now.toLocalDate());

        // Then
        then(restaurantRepository).should().findById(restaurantId);
        then(reservationSlotService).should().findReservationSlotsByDate(restaurantId, now.toLocalDate());

        assertThat(result.reservationSlots()).hasSize(reservationSlots.size());
    }

    @Test
    @DisplayName("성공 - 식당 Id를 받아 식당을 조회하면 operatingHours 포함한 식당 DTO를 반환한다.")
    void getRestaurantDetailWithOperatingHoursTest() {
        // Given
        Long restaurantId = 1L;
        RestaurantEntity restaurantEntity = createRestaurant("테스트 식당");
        ReflectionTestUtils.setField(restaurantEntity, "id", restaurantId);
        given(restaurantRepository.findById(restaurantId)).willReturn(Optional.of(restaurantEntity));

        RestaurantOperatingHours restaurantOperatingHours = createRestaurantOperatingHours(restaurantId);
        given(restaurantOperatingHoursRepository.findByRestaurantId(restaurantId)).willReturn(Optional.of(restaurantOperatingHours));

        LocalDateTime now = LocalDateTime.now();

        // When
        RestaurantDetailDto result = restaurantService.getRestaurantDetail(restaurantId, now.toLocalDate());

        // Then
        then(restaurantRepository).should().findById(restaurantId);
        then(restaurantOperatingHoursRepository).should().findByRestaurantId(restaurantId);

        assertThat(result.restaurant().operatingHours()).isNotNull();
        assertThat(result.restaurant().operatingHours()).hasSize(1);
    }

    private RestaurantEntity createRestaurant(String name) {
        return createRestaurant(name, null);
    }

    private RestaurantEntity createRestaurant(String name, String category) {
        return RestaurantEntity.builder()
                .name(name)
                .category(category)
                .build();
    }

    private MenuEntity createMenu(RestaurantEntity restaurantEntity, String name) {
        return MenuEntity.of(restaurantEntity, name, null, null, null);
    }

    private ReservationSlotDto createReservationSlot(LocalDateTime time) {
        return ReservationSlotDto.builder()
                .slotId(1L)
                .time(time)
                .remainingCapacity(5)
                .build();
    }

    private RestaurantOperatingHours createRestaurantOperatingHours(Long restaurantId) {
        OperatingHour monday = OperatingHour.builder()
                .dayOfWeek("수")
                .isClosed(true)
                .build();

        return RestaurantOperatingHours.of(restaurantId, List.of(monday));
    }

    private ReviewDto createReveiewDto(String comment) {
        return ReviewDto.builder().comment(comment).build();
    }

}
