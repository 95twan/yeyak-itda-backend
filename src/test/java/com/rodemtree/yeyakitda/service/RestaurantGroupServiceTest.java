package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.dto.RestaurantGroupDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.RestaurantThemeMappingEntity;
import com.rodemtree.yeyakitda.entity.ThemeEntity;
import com.rodemtree.yeyakitda.mapper.RestaurantGroupMapper;
import com.rodemtree.yeyakitda.mapper.RestaurantMapper;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import com.rodemtree.yeyakitda.repository.ThemeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.ui.context.Theme;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
@DisplayName("비즈니스 로직 - 식당 그룹")
class RestaurantGroupServiceTest {

    private RestaurantGroupService restaurantGroupService;

    @Mock
    private ThemeRepository themeRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private Clock clock;

    private RestaurantGroupMapper restaurantGroupMapper = Mappers.getMapper(RestaurantGroupMapper.class);
    private RestaurantMapper restaurantMapper = Mappers.getMapper(RestaurantMapper.class);

    @BeforeEach
    void setUp() {
        restaurantGroupService = new RestaurantGroupService(themeRepository, restaurantRepository, restaurantGroupMapper, restaurantMapper, clock);
    }

    @Test
    @DisplayName("성공 - 테마별로 그룹화된 식당 목록을 DTO로 반환한다.")
    void findThemeGroupedRestaurantsTest() {
        // Given
        LocalDateTime fixedNow = LocalDateTime.of(2025, 9, 8, 18, 0, 0);
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        Instant fixedInstant = fixedNow.atZone(seoulZone).toInstant();
        given(clock.instant()).willReturn(fixedInstant);
        given(clock.getZone()).willReturn(seoulZone);

        ThemeEntity popularTheme = createTheme("인기 식당 추천", fixedNow.minusDays(1), fixedNow.plusDays(1));
        ThemeEntity newTheme = createTheme("신규 등록 식당", fixedNow.minusDays(2), fixedNow.plusDays(2));

        List<ThemeEntity> themeEntities = List.of(popularTheme, newTheme);
        given(themeRepository.findInProgressThemes(fixedNow)).willReturn(themeEntities);

        RestaurantEntity restaurantA = createRestaurant("빕스");
        RestaurantEntity restaurantB = createRestaurant("애슐리");
        RestaurantEntity restaurantC = createRestaurant("새로운 파스타집");

        given(restaurantRepository.findTop10ByTheme(popularTheme)).willReturn(List.of(restaurantA, restaurantB));
        given(restaurantRepository.findTop10ByTheme(newTheme)).willReturn(List.of(restaurantC));

        // When
        List<RestaurantGroupDto> result = restaurantGroupService.findThemeGroupedRestaurants();

        // Then
        assertThat(result).hasSize(2);
        // '인기 식당' 그룹 검증
        RestaurantGroupDto popularGroup = result.stream().filter(g -> g.title().equals("인기 식당 추천")).findFirst().orElse(null);
        assertThat(popularGroup).isNotNull();
        assertThat(popularGroup.restaurants()).hasSize(2);
        assertThat(popularGroup.restaurants().stream().map(RestaurantDto::name).toList())
                .containsExactlyInAnyOrder("빕스", "애슐리");

        // '신규 등록' 그룹 검증
        RestaurantGroupDto newGroup = result.stream().filter(g -> g.title().equals("신규 등록 식당")).findFirst().orElse(null);
        assertThat(newGroup).isNotNull();
        assertThat(newGroup.restaurants()).hasSize(1);
        assertThat(newGroup.restaurants().get(0).name()).isEqualTo("새로운 파스타집");

    }

    private ThemeEntity createTheme(String title, LocalDateTime startDate, LocalDateTime endDate) {
        return ThemeEntity.of(title, startDate, endDate);
    }

    private RestaurantEntity createRestaurant(String name) {
        return RestaurantEntity.builder()
                .name(name)
                .build();
    }
}
