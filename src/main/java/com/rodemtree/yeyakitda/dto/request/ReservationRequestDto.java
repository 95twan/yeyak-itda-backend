package com.rodemtree.yeyakitda.dto.request;

public record ReservationRequestDto(
        Long slotId,
        Integer headCount
) {
    public static ReservationRequestDto of(Long slotId, Integer headCount) {
        return new ReservationRequestDto(slotId, headCount);
    }
}
