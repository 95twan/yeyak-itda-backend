package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.dto.RestaurantGroupDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.RestaurantThemeMappingEntity;
import com.rodemtree.yeyakitda.entity.ThemeEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.mapper.RestaurantGroupMapper;
import com.rodemtree.yeyakitda.mapper.RestaurantMapper;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import com.rodemtree.yeyakitda.repository.RestaurantThemeMappingRepository;
import com.rodemtree.yeyakitda.repository.ThemeRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional // 테스트 후 데이터 롤백
@DisplayName("성능 테스트 - 식당 그룹 서비스")
class RestaurantGroupServicePerformanceTest {

    @Autowired
    private RestaurantGroupService restaurantGroupService;

    @Autowired
    private ThemeRepository themeRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RestaurantThemeMappingRepository restaurantThemeMappingRepository;

    @BeforeEach
    void setUp() {
        UserEntity testUser = UserEntity.builder()
                .email("owner@test.com")
                .password("password")
                .name("Test Owner")
                .nickname("testowner")
                .address("Test Address")
                .phoneNumber("010-0000-0000")
                .build();
        testUser.setDefaultRole();
        userRepository.save(testUser);

        int themeCount = 10;
        int restaurantPerTheme = 100;

        LocalDateTime now = LocalDateTime.now();
        List<ThemeEntity> themes = new ArrayList<>();
        for (int i = 0; i < themeCount; i++) {
            themes.add(ThemeEntity.of("테마 " + i, now.minusDays(1), now.plusDays(1)));
        }
        themeRepository.saveAll(themes);

        for (ThemeEntity theme : themes) {
            List<RestaurantEntity> restaurants = new ArrayList<>();
            for (int i = 0; i < restaurantPerTheme; i++) {
                restaurants.add(RestaurantEntity.builder()
                        .user(testUser)
                        .name(theme.getTitle() + " - 식당 " + i)
                        .category("한식")
                        .phoneNumber("010-1234-1234")
                        .address("경기도 구리시")
                        .description("맛집입니다.")
                        .build());
            }
            restaurantRepository.saveAll(restaurants);

            List<RestaurantThemeMappingEntity> mappings = new ArrayList<>();
            for (RestaurantEntity restaurant : restaurants) {
                mappings.add(RestaurantThemeMappingEntity.of(restaurant, theme));
            }
            restaurantThemeMappingRepository.saveAll(mappings);
        }
    }

    @Test
    @DisplayName("성공 - [캐시 적용 전] 대량 데이터 조회 시 실행 시간을 측정한다.")
    void measureFindThemeGroupedRestaurantsPerformanceTestBeforeCaching() {
        // Given

        // When
        long startTime = System.currentTimeMillis();
        restaurantGroupService.findThemeGroupedRestaurants();
        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;

        // Then
        // [Before Caching] Execution time: 139ms
        System.out.println("[Before Caching] Execution time: " + resultTime + "ms");

    }

    @Test
    @DisplayName("성공 - [캐시 적용 후] 대량 데이터 조회 시 실행 시간을 측정한다.")
    void measureFindThemeGroupedRestaurantsPerformanceTestAfterCaching() {
        // Given
        restaurantGroupService.findThemeGroupedRestaurants();

        // When
        long startTime = System.currentTimeMillis();
        restaurantGroupService.findThemeGroupedRestaurants();
        long endTime = System.currentTimeMillis();
        long resultTime = endTime - startTime;

        // Then
        // [After Caching] Execution time: 2ms
        System.out.println("[After Caching] Execution time: " + resultTime + "ms");

    }
}
