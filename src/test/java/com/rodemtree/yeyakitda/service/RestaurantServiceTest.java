package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.mapper.RestuarantMapper;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.data.mapping.PropertyReferenceException;
import org.springframework.data.util.TypeInformation;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
class RestaurantServiceTest {

    @InjectMocks
    private RestaurantService restaurantService;

    @Mock
    private RestaurantRepository restaurantRepository;

    @Mock
    private RestuarantMapper restuarantMapper;

    @Test
    @DisplayName("성공 - 페이징 정보를 받아 식당 목록을 조회하면 식당 DTO 페이지를 반환한다.")
    void getRestaurantListTest() {
        // Given
        List<RestaurantEntity> restaurantEntityList = IntStream.range(1, 11)
                .mapToObj(i -> createRestaurant("restaurant" + i))
                .toList();
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());
        Page<RestaurantEntity> restaurantEntityPage = new PageImpl<>(restaurantEntityList, pageable, 10);
        given(restaurantRepository.findByCategories(null, pageable)).willReturn(restaurantEntityPage);
        given(restuarantMapper.restaurantEntityToRestaurantDto(any(RestaurantEntity.class))).will(invocation -> {
            RestaurantEntity restaurantEntity = invocation.getArgument(0);
            return createRestaurantDto(restaurantEntity.getName());
        });

        // When
        Page<RestaurantDto> result = restaurantService.getRestaurantList(null, pageable);

        // Then
        then(restaurantRepository).should().findByCategories(null, pageable);

        assertThat(result.getTotalElements()).isEqualTo(10);
        assertThat(result.getContent().get(0).name()).isEqualTo("restaurant1");

    }

    @Test
    @DisplayName("실패 - 유효하지 않은 필드로 정렬을 요청하면, PropertyReferenceException을 던진다.")
    void getRestaurantListWithInvalidSortTest() {
        // Given
        Pageable pageable = PageRequest.of(0, 10, Sort.by("invalid").descending());
        given(restaurantRepository.findByCategories(null, pageable))
                .willThrow(new PropertyReferenceException("invalid", TypeInformation.of(RestaurantEntity.class), Collections.emptyList()));

        // When & Then
        assertThatThrownBy(() -> restaurantService.getRestaurantList(null, pageable))
                .isInstanceOf(PropertyReferenceException.class);

        then(restaurantRepository).should().findByCategories(null, pageable);
    }

    @Test
    @DisplayName("성공 - 카테고리로 식당 목록을 조회하면 해당 카테고리의 식당 Dto를 반환한다.")
    void getRestaurantListByCategoryTest() {
        // Given
        Set<String> categories = Set.of("한식");
        Pageable pageable = PageRequest.of(0, 10, Sort.by("rating").descending());
        Page<RestaurantEntity> restaurantEntityPage = new PageImpl<>(List.of(createRestaurant("테스트 식당", "한식,중식")), pageable, 1);
        given(restaurantRepository.findByCategories(categories, pageable)).willReturn(restaurantEntityPage);
        given(restuarantMapper.restaurantEntityToRestaurantDto(any(RestaurantEntity.class))).will(invocation -> {
            RestaurantEntity restaurantEntity = invocation.getArgument(0);
            return createRestaurantDto(restaurantEntity.getName(), restaurantEntity.getCategory());
        });

        // When
        Page<RestaurantDto> result = restaurantService.getRestaurantList(categories, pageable);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).category()).contains(categories);

        then(restaurantRepository).should().findByCategories(categories, pageable);
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

    private RestaurantDto createRestaurantDto(String name) {
        return createRestaurantDto(name, null);
    }


    private RestaurantDto createRestaurantDto(String name, String category) {
        return new RestaurantDto(null, name, null, null, null, category, null);
    }
}
