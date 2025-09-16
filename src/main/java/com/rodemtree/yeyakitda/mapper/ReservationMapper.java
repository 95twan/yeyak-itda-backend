package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.ReservationDto;
import com.rodemtree.yeyakitda.entity.ReservationEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationMapper {

    @Mapping(source = "id", target = "reservationId")
    @Mapping(source = "restaurant.id", target = "restaurantId")
    @Mapping(source = "restaurant.name", target = "restaurantName")
    @Mapping(source = "reservationSlot.slotAt", target = "reservationTime")
    @Mapping(source = "status.value", target = "status")
    ReservationDto reservationEntityToReservationDto(ReservationEntity entity);

    List<ReservationDto> reservationEntitiesToReservationDtos(List<ReservationEntity> entityList);
}
