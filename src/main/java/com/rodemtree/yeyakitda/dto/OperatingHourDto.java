package com.rodemtree.yeyakitda.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record OperatingHourDto(
        String dayOfWeek,
        String openingTime,
        String closingTime,
        String breakTimeStart,
        String breakTimeEnd,
        Boolean isClosed
) {
}
