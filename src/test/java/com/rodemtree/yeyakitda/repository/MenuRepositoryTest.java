package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.config.AbstractMySQLContainer;
import com.rodemtree.yeyakitda.config.JpaConfig;
import com.rodemtree.yeyakitda.entity.MenuEntity;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@DisplayName("리포지토리 - Menu")
class MenuRepositoryTest extends AbstractMySQLContainer {

    @Autowired
    private MenuRepository menuRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    private Long restaurantId;

    @BeforeEach
    void setUp() {
        UserEntity user = userRepository.save(createUser());
        RestaurantEntity restaurant1 = restaurantRepository.save(createRestaurant(user, "테스트 식당1"));
        RestaurantEntity restaurant2 = restaurantRepository.save(createRestaurant(user, "테스트 식당2"));

        this.restaurantId = restaurant1.getId();

        List<MenuEntity> menus = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            MenuEntity menu = MenuEntity.of(restaurant1, "메뉴" + i, "설명" + i, "http://test" + i, i * 1000);
            menus.add(menu);
        }
        for (int i = 3; i < 5; i++) {
            MenuEntity menu = MenuEntity.of(restaurant2, "메뉴" + i, "설명" + i, "http://test" + i, i * 1000);
            menus.add(menu);
        }
        menuRepository.saveAll(menus);
    }

    @Test
    @DisplayName("성공 - 레스토랑 Id로 메뉴 목록을 검색한다.")
    void findByRestaurant_IdTest() {
        // Given


        // When
        List<MenuEntity> menus = menuRepository.findByRestaurant_Id(restaurantId);

        // Then
        assertThat(menus.size()).isEqualTo(3);
        assertThat(menus.get(0).getRestaurant().getId()).isEqualTo(restaurantId);
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

    private RestaurantEntity createRestaurant(UserEntity user, String restaurantName) {
        return RestaurantEntity.builder()
                .name(restaurantName)
                .user(user)
                .description("test desc")
                .phoneNumber("010-1234-1234")
                .address("경기도")
                .category("중식")
                .build();
    }
}
