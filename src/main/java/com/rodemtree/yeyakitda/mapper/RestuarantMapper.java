package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface RestuarantMapper {

    @Mapping(source = "restaurantImage.imageUrl", target = "thumbnailImageUrl")
    RestaurantDto restaurantEntityToRestaurantDto(RestaurantEntity entity);

}
