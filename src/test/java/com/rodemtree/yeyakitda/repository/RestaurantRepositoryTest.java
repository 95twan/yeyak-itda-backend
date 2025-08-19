package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.config.JpaConfig;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import(JpaConfig.class)
@DisplayName("리포지토리 - Restaurant")
class RestaurantRepositoryTest {

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

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

    private RestaurantEntity createRestaurant(UserEntity user) {
        return RestaurantEntity.builder()
                .name("test-name")
                .user(user)
                .description("test-description")
                .phoneNumber("010-1234-1234")
                .address("address")
                .category("한식")
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
