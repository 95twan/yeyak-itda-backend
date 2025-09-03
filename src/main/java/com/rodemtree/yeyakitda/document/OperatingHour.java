package com.rodemtree.yeyakitda.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatingHour {
    private String dayOfWeek;
    private String openingTime;
    private String closingTime;
    private String breakTimeStart;
    private String breakTimeEnd;
    private Boolean isClosed;
}
