package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.ReservationSlotDto;
import com.rodemtree.yeyakitda.entity.ReservationSlotEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ReservationSlotMapper {

    @Mapping(source = "id", target = "slotId")
    @Mapping(source = "slotAt", target = "time")
    ReservationSlotDto reservationSlotEntityToReservationSlotDto(ReservationSlotEntity entity);

    List<ReservationSlotDto> reservationSlotEntitiesToReservationSlotDtos(List<ReservationSlotEntity> entities);
}
