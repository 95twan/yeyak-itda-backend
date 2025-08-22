package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.MenuDto;
import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.dto.RestaurantInfoDto;
import com.rodemtree.yeyakitda.entity.MenuEntity;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.RestaurantImageEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    List<MenuDto> menuEntityListToMenuDtoList(List<MenuEntity> entity);

    MenuDto menuEntityToMenuDto(MenuEntity entity);

}
