package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.config.AbstractMySQLContainer;
import com.rodemtree.yeyakitda.config.JpaConfig;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.RestaurantThemeMappingEntity;
import com.rodemtree.yeyakitda.entity.ThemeEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@DisplayName("리포지토리 - Restaurant")
class RestaurantRepositoryTest extends AbstractMySQLContainer {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private RestaurantThemeMappingRepository restaurantThemeMappingRepository;

    @Test
    @DisplayName("실패 - 잘못된 Sort parameter가 주어지면 PropertyReferenceException을 던진다.")
    void findAllWithInvalidSortTest() {
        // Given
        Sort sort = Sort.by("invalid").descending();

        // When & Then
        assertThatThrownBy(() -> restaurantRepository.findAll(sort))
                .isInstanceOf(PropertyReferenceException.class);
    }

    @Test
    @DisplayName("성공 - 검색조건이 없으면 전체 식당 목록 조회")
    void searchWithoutConditionTest() {
        // Given
        UserEntity user = userRepository.save(createUser());
        restaurantRepository.save(createRestaurant(user));

        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());

        // When
        Page<RestaurantEntity> result = restaurantRepository.search(condition, pageable);

        // Then
        assertThat(result.getContent().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("성공 - category로 식당 목록 조회")
    void searchWithCategoriesTest() {
        // Given
        UserEntity user = userRepository.save(createUser());
        restaurantRepository.save(createRestaurant(user, "한식,양식"));
        restaurantRepository.save(createRestaurant(user, "양식"));
        restaurantRepository.save(createRestaurant(user, "한식"));
        restaurantRepository.save(createRestaurant(user, "중식"));

        Set<String> categories = Set.of("한식", "양식");
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().categories(categories).build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());

        // When
        Page<RestaurantEntity> result = restaurantRepository.search(condition, pageable);

        // Then
        assertThat(result.getContent().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("성공 - keyword로 식당 목록 조회")
    void searchWithKeywordTest() {
        // Given
        UserEntity user = userRepository.save(createUser());
        restaurantRepository.save(createRestaurant(user, "테스트 식당", "서울 강남구", "상세1"));
        restaurantRepository.save(createRestaurant(user, "강남 맛집", "서울 테스트구", "상세2"));
        restaurantRepository.save(createRestaurant(user, "마포 주먹고기", "서울 마포구", "상세3"));
        restaurantRepository.save(createRestaurant(user, "용산 김밥천국", "서울 용산구", "테스트1"));

        String keyword = "테스트";
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().keyword(keyword).build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());

        // When
        Page<RestaurantEntity> result = restaurantRepository.search(condition, pageable);

        // Then
        assertThat(result.getContent().size()).isEqualTo(3);
    }

    @Test
    @DisplayName("성공 - 카테고리와 keyword로 식당 목록 조회")
    void searchWithCategoriesAndKeywordTest() {
        // Given
        UserEntity user = userRepository.save(createUser());
        restaurantRepository.save(createRestaurant(user, "테스트 식당", "서울 강남구", "상세1", "한식,약식"));
        restaurantRepository.save(createRestaurant(user, "강남 맛집", "서울 테스트구", "상세2", "한식"));
        restaurantRepository.save(createRestaurant(user, "마포 주먹고기", "서울 마포구", "상세3", "양식"));
        restaurantRepository.save(createRestaurant(user, "용산 김밥천국", "서울 용산구", "테스트1", "중식"));

        String keyword = "테스트";
        Set<String> categories = Set.of("한식", "양식");
        RestaurantSearchConditionDto condition = RestaurantSearchConditionDto.builder().keyword(keyword).categories(categories).build();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());

        // When
        Page<RestaurantEntity> result = restaurantRepository.search(condition, pageable);

        // Then
        assertThat(result.getContent().size()).isEqualTo(2);
    }

    @Test
    @DisplayName("성공 - 테마 이름으로 식당 목록을 조회한다.")
    void findByThemeTest() {
        // Given
        UserEntity user = userRepository.save(createUser());
        ThemeEntity popularTheme = themeRepository.save(createTheme("인기 식당"));
        ThemeEntity newTheme = themeRepository.save(createTheme("신규 식당"));

        RestaurantEntity restaurant1 = restaurantRepository.save(createRestaurant(user, "인기 식당 1", "한식"));
        RestaurantEntity restaurant2 = restaurantRepository.save(createRestaurant(user, "인기 식당 2", "한식"));
        RestaurantEntity restaurant3 = restaurantRepository.save(createRestaurant(user, "신규 식당 1", "한식"));

        restaurantThemeMappingRepository.save(RestaurantThemeMappingEntity.of(restaurant1, popularTheme));
        restaurantThemeMappingRepository.save(RestaurantThemeMappingEntity.of(restaurant2, popularTheme));
        restaurantThemeMappingRepository.save(RestaurantThemeMappingEntity.of(restaurant3, newTheme));

        String theme = "인기 식당";
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<RestaurantEntity> result = restaurantRepository.findByTheme(theme, pageable);

        // Then
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getContent()).extracting(RestaurantEntity::getName)
                .containsExactlyInAnyOrder("인기 식당 1", "인기 식당 2");
    }


    private RestaurantEntity createRestaurant(UserEntity user) {
        return createRestaurant(user, "test-category");
    }

    private RestaurantEntity createRestaurant(UserEntity user, String category) {
        return createRestaurant(user, "test-name", category);
    }

    private RestaurantEntity createRestaurant(UserEntity user, String name, String category) {
        return createRestaurant(user, name, "test-address", "test-description", category);
    }

    private RestaurantEntity createRestaurant(UserEntity user, String name, String address, String description) {
        return createRestaurant(user, name, address, description, "test-category");
    }

    private RestaurantEntity createRestaurant(UserEntity user, String name, String address, String description, String category) {
        return RestaurantEntity.builder()
                .name(name)
                .user(user)
                .description(description)
                .phoneNumber("010-1234-1234")
                .address(address)
                .category(category)
                .build();
    }

    private UserEntity createUser() {
        UserEntity userEntity = UserEntity.builder()
                .email("test@test.com")
                .name("홍길동")
                .address("경기도 구리시")
                .password("encoded-pw")
                .phoneNumber("010-1234-1234")
                .nickname("test")
                .build();
        userEntity.setDefaultRole();
        return userEntity;
    }

    private ThemeEntity createTheme(String title) {
        return ThemeEntity.of(title, LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
    }
}
