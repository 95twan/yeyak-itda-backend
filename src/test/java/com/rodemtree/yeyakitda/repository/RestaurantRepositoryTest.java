package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.config.JpaConfig;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
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

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("리포지토리 - Restaurant")
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        restaurantRepository.deleteAll();
    }

    @Test
    @DisplayName("성공 - RestaurantEntity를 저장한다.")
    void saveTest() {
        // Given
        UserEntity user = createUser();
        UserEntity savedUser = userRepository.save(user);
        RestaurantEntity restaurant = createRestaurant(savedUser);

        // When
        RestaurantEntity savedRestaurant = restaurantRepository.save(restaurant);

        // Then
        assertThat(savedRestaurant.getId()).isNotNull();
        assertThat(savedRestaurant.getName()).isEqualTo(restaurant.getName());
        assertThat(savedRestaurant.getUser()).isEqualTo(restaurant.getUser());
        assertThat(savedRestaurant.getDescription()).isEqualTo(restaurant.getDescription());
        assertThat(savedRestaurant.getPhoneNumber()).isEqualTo(restaurant.getPhoneNumber());
        assertThat(savedRestaurant.getAddress()).isEqualTo(restaurant.getAddress());
        assertThat(savedRestaurant.getCategory()).isEqualTo(restaurant.getCategory());
        assertThat(savedRestaurant.getRating()).isEqualTo(0f); //default value 검증
    }

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
    @DisplayName("성공 - category로 조회")
    void findByCategoryInTest() {
        // Given
        UserEntity user = userRepository.save(createUser());
        restaurantRepository.save(createRestaurant(user));
        Set<String> categories = Set.of("한식", "양식");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());

        // When
        Page<RestaurantEntity> result = restaurantRepository.findByCategories(categories, pageable);

        // Then
        assertThat(result.getContent().size()).isEqualTo(1);
    }

    @Test
    @DisplayName("성공 - category가 없으면 전체 조회")
    void findAllTest() {
        // Given
        UserEntity user = userRepository.save(createUser());
        restaurantRepository.save(createRestaurant(user));
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());

        // When
        Page<RestaurantEntity> result = restaurantRepository.findByCategories(null, pageable);

        // Then
        assertThat(result.getContent().size()).isEqualTo(1);
    }


    private RestaurantEntity createRestaurant(UserEntity user) {
        return RestaurantEntity.builder()
                .name("test-name")
                .user(user)
                .description("test-description")
                .phoneNumber("010-1234-1234")
                .address("address")
                .category("한식,양식,중식")
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
}
