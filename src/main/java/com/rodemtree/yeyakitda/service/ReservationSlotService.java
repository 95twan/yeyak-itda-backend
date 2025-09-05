package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.ReservationSlotDto;
import com.rodemtree.yeyakitda.mapper.ReservationSlotMapper;
import com.rodemtree.yeyakitda.repository.ReservationSlotRepository;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReservationSlotService {

    private final ReservationSlotRepository reservationSlotRepository;
    private final RestaurantRepository restaurantRepository;
    private final ReservationSlotMapper reservationSlotMapper;

    private static final int CUTOFF_HOUR = 6;

    public List<ReservationSlotDto> findReservationSlotsByDate(Long restaurnatId, LocalDate date) {
        LocalDateTime startDate = date.atTime(CUTOFF_HOUR, 0);
        LocalDateTime endDate = date.plusDays(1).atTime(CUTOFF_HOUR, 0);

        if (! restaurantRepository.existsById(restaurnatId)) {
            throw new EntityNotFoundException("해당 식당을 찾을 수 없습니다.");
        }

        return reservationSlotMapper.reservationSlotEntitiesToReservationSlotDtos(reservationSlotRepository.findByRestaurant_IdAndSlotAtGreaterThanEqualAndSlotAtLessThan(restaurnatId, startDate, endDate));
    }
}
