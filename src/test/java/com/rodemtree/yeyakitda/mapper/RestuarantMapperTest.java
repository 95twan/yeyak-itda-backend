package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.RestaurantImageEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class RestuarantMapperTest {

    private RestuarantMapper restuarantMapper = Mappers.getMapper(RestuarantMapper.class);

    @Test
    @DisplayName("성공 - restaurantEntity -> restaurantDto 변환")
    void restaurantEntityToRestaurantDtoTest() {
        // Given
        RestaurantEntity restaurantEntity = RestaurantEntity.builder()
                .name("테스트 식당")
                .category("한식")
                .description("테스트 식당 상세 내용")
                .address("경기도 구리시")
                .build();
        RestaurantImageEntity restaurantImageEntity = RestaurantImageEntity.of(restaurantEntity, "https://test.com/image.png");
        restaurantEntity.updateRestaurantThumbnailImage(restaurantImageEntity);

        // When
        RestaurantDto restaurantDto = restuarantMapper.restaurantEntityToRestaurantDto(restaurantEntity);

        // Then
        assertThat(restaurantDto.name()).isEqualTo(restaurantEntity.getName());
        assertThat(restaurantDto.thumbnailImageUrl()).isEqualTo(restaurantEntity.getRestaurantImage().getImageUrl());
        assertThat(restaurantDto.description()).isEqualTo(restaurantEntity.getDescription());
        assertThat(restaurantDto.address()).isEqualTo(restaurantEntity.getAddress());
        assertThat(restaurantDto.category()).contains(restaurantEntity.getCategory());
        assertThat(restaurantDto.rating()).isEqualTo(restaurantEntity.getRating());
    }
}
