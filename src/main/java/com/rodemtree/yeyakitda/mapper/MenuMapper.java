package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.MenuDto;
import com.rodemtree.yeyakitda.entity.MenuEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface MenuMapper {

    List<MenuDto> menuEntitiesToMenuDtos(List<MenuEntity> entity);

    MenuDto menuEntityToMenuDto(MenuEntity entity);

}
