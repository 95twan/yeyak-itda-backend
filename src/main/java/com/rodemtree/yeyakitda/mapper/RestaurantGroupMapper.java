package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.dto.RestaurantGroupDto;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.ThemeEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(
        componentModel = "spring"
)
public interface RestaurantGroupMapper {

    @Mapping(target = "id", source = "themeEntity.id")
    @Mapping(target = "title", source = "themeEntity.title")
    @Mapping(target = "restaurants", source = "restaurantDtos")
    RestaurantGroupDto group(ThemeEntity themeEntity, List<RestaurantDto> restaurantDtos);

}
