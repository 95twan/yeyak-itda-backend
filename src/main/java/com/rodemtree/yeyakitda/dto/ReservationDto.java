package com.rodemtree.yeyakitda.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReservationDto(
        Long reservationId,
        Long restaurantId,
        String restaurantName,
        LocalDateTime reservationTime,
        Integer headCount,
        String status
) {
}
