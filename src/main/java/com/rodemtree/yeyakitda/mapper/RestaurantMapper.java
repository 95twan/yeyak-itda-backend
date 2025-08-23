package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.RestaurantInfoDto;
import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.RestaurantImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface RestaurantMapper {

    @Mapping(source = "thumbnailImage.imageUrl", target = "thumbnailImageUrl")
    RestaurantDto restaurantEntityToRestaurantDto(RestaurantEntity entity);

    @Mapping(source = "entity.user.id", target = "userId")
    @Mapping(source = "images", target = "imageUrls")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "operatingHours", ignore = true)
    RestaurantInfoDto restaurantEntityToRestaurantInfoDto(RestaurantEntity entity, List<RestaurantImageEntity> images);

    default List<String> mapImageUrls(List<RestaurantImageEntity> images) {
        return images.stream().map(RestaurantImageEntity::getImageUrl).toList();
    }
}
