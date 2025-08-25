package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.ReservationSlotDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationSlotService {

    public List<ReservationSlotDto> findReservationSlotsByDate(Long restaurnatId, LocalDate date) {
        return null;
    }
}
