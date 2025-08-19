package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface RestuarantMapper {

    RestaurantDto restaurantEntityToRestaurantDto(RestaurantEntity entity);
}
