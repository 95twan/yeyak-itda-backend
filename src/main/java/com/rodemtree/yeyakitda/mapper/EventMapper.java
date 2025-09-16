package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.EventDto;
import com.rodemtree.yeyakitda.dto.MenuDto;
import com.rodemtree.yeyakitda.entity.EventEntity;
import com.rodemtree.yeyakitda.entity.MenuEntity;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface EventMapper {

    EventDto eventEntityToEventDto(EventEntity entity);

}
