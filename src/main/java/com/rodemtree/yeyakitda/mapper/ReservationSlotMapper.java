package com.rodemtree.yeyakitda.mapper;

import com.rodemtree.yeyakitda.dto.ReservationSlotDto;
import com.rodemtree.yeyakitda.dto.ReviewDto;
import com.rodemtree.yeyakitda.entity.ReservationSlotEntity;
import com.rodemtree.yeyakitda.entity.ReviewEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ReservationSlotMapper {

    @Mapping(source = "id", target = "slotId")
    @Mapping(source = "slotAt", target = "time")
    ReservationSlotDto reservationSlotEntityToReservationSlotDto(ReservationSlotEntity entity);
}
