package com.rodemtree.yeyakitda.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;

@Builder
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
