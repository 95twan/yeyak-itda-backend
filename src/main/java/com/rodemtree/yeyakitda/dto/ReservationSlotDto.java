package com.rodemtree.yeyakitda.dto;

import lombok.Builder;

import java.time.LocalDateTime;

@Builder
public record ReservationSlotDto(
        Long slotId,
        LocalDateTime time,
        Integer remainingCapacity
) {
}
