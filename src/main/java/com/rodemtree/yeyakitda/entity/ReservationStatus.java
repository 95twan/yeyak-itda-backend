package com.rodemtree.yeyakitda.entity;

import lombok.Getter;

public enum ReservationStatus {
    RESERVED("예약 완료"),
    CANCELLED("예약 취소"),
    COMPLETED("이용 완료"),
    WAITING("예약 대기");

    @Getter
    private final String value;

    ReservationStatus(String value) {
        this.value = value;
    }
}
