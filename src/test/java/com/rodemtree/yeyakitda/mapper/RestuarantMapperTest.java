package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.RestaurantInfoDto;
import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.RestaurantImageEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


@ExtendWith(MockitoExtension.class)
class RestuarantMapperTest {

    private final RestuarantMapper restuarantMapper = Mappers.getMapper(RestuarantMapper.class);

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
        assertThat(restaurantDto.thumbnailImageUrl()).isEqualTo(restaurantEntity.getThumbnailImage().getImageUrl());
        assertThat(restaurantDto.description()).isEqualTo(restaurantEntity.getDescription());
        assertThat(restaurantDto.address()).isEqualTo(restaurantEntity.getAddress());
        assertThat(restaurantDto.category()).contains(restaurantEntity.getCategory());
        assertThat(restaurantDto.rating()).isEqualTo(restaurantEntity.getRating());
    }

    @Test
    @DisplayName("성공 - restaurantEntity -> restaurantDetailDto 변환")
    void restaurantEntityToRestaurantInfoDtoTest() {
        // Given
        RestaurantEntity restaurantEntity = RestaurantEntity.builder()
                .name("테스트 식당")
                .category("한식")
                .description("테스트 식당 상세 내용")
                .phoneNumber("010-1234-1234")
                .address("경기도 구리시")
                .build();
        RestaurantImageEntity restaurantImageEntity1 = RestaurantImageEntity.of(restaurantEntity, "https://test.com/image.png");
        RestaurantImageEntity restaurantImageEntity2 = RestaurantImageEntity.of(restaurantEntity, "https://test.com/image.png");
        List<RestaurantImageEntity> restaurantImages = List.of(restaurantImageEntity1, restaurantImageEntity2);
        restaurantEntity.updateRestaurantThumbnailImage(restaurantImageEntity1);

        // When
        RestaurantInfoDto restaurantInfoDto = restuarantMapper.restaurantEntityToRestaurantInfoDto(restaurantEntity, restaurantImages);

        // Then
        assertThat(restaurantInfoDto.name()).isEqualTo(restaurantEntity.getName());
        assertThat(restaurantInfoDto.description()).isEqualTo(restaurantEntity.getDescription());
        assertThat(restaurantInfoDto.address()).isEqualTo(restaurantEntity.getAddress());
        assertThat(restaurantInfoDto.category()).contains(restaurantEntity.getCategory());
        assertThat(restaurantInfoDto.phoneNumber()).isEqualTo(restaurantEntity.getPhoneNumber());
        assertThat(restaurantInfoDto.rating()).isEqualTo(restaurantEntity.getRating());
        assertThat(restaurantInfoDto.imageUrls()).hasSize(2);
    }
}
