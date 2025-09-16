package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.document.OperatingHour;
import com.rodemtree.yeyakitda.dto.OperatingHourDto;
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

    List<RestaurantDto> restaurantEntitiesToRestaurantDtos(List<RestaurantEntity> entities);

    @Mapping(source = "entity.user.id", target = "userId")
    @Mapping(source = "images", target = "imageUrls")
    @Mapping(target = "tags", ignore = true)
    RestaurantInfoDto restaurantEntityToRestaurantInfoDto(RestaurantEntity entity, List<RestaurantImageEntity> images, List<OperatingHour> operatingHours);

    default List<String> mapImageUrls(List<RestaurantImageEntity> images) {
        return images.stream().map(RestaurantImageEntity::getImageUrl).toList();
    }

}
