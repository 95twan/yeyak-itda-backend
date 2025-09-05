package com.rodemtree.yeyakitda.service;


import com.rodemtree.yeyakitda.dto.ReservationSlotDto;
import com.rodemtree.yeyakitda.entity.ReservationSlotEntity;
import com.rodemtree.yeyakitda.mapper.ReservationSlotMapper;
import com.rodemtree.yeyakitda.repository.ReservationSlotRepository;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@DisplayName("비즈니스 로직 - 리뷰 슬롯")
@ExtendWith(MockitoExtension.class)
class ReservationSlotServiceTest {

    private ReservationSlotService reservationSlotService;

    @Mock
    private ReservationSlotRepository reservationSlotRepository;

    @Mock
    private RestaurantRepository restaurantRepository;

    private final ReservationSlotMapper reservationSlotMapper = Mappers.getMapper(ReservationSlotMapper.class);

    private static final int CUTOFF_HOUR = 6;

    @BeforeEach
    public void setUp() {
        reservationSlotService = new ReservationSlotService(reservationSlotRepository, restaurantRepository, reservationSlotMapper);
    }

    @Test
    @DisplayName("성공 - 식당 Id와 날짜가 주어지면, 해당 날짜의 영업 시간 내 예약 슬롯을 DTO로 반환한다.")
    void findReservationSlotsByDateTest() {
        // Given
        Long restaurantId = 1L;
        LocalDate date = LocalDate.of(2025, 8, 26);
        LocalDateTime expectedStartDateTime = date.atTime(CUTOFF_HOUR, 0);
        LocalDateTime expectedEndDateTime = date.plusDays(1).atTime(CUTOFF_HOUR, 0);
        ReservationSlotEntity reservationSlot1 = createResevationSlot(expectedStartDateTime.plusHours(1));
        ReservationSlotEntity reservationSlot2 = createResevationSlot(expectedStartDateTime.plusHours(2));
        List<ReservationSlotEntity> reservationSlots = List.of(reservationSlot1, reservationSlot2);
        given(restaurantRepository.existsById(restaurantId)).willReturn(true);
        given(reservationSlotRepository.findByRestaurant_IdAndSlotAtGreaterThanEqualAndSlotAtLessThan(anyLong(), any(), any())).willReturn(reservationSlots);

        // When
        List<ReservationSlotDto> result = reservationSlotService.findReservationSlotsByDate(restaurantId, date);

        // Then
        ArgumentCaptor<LocalDateTime> startDateTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        ArgumentCaptor<LocalDateTime> endDateTimeCaptor = ArgumentCaptor.forClass(LocalDateTime.class);
        then(reservationSlotRepository).should().findByRestaurant_IdAndSlotAtGreaterThanEqualAndSlotAtLessThan(eq(restaurantId), startDateTimeCaptor.capture(), endDateTimeCaptor.capture());

        assertThat(startDateTimeCaptor.getValue()).isEqualTo(expectedStartDateTime);
        assertThat(endDateTimeCaptor.getValue()).isEqualTo(expectedEndDateTime);

        assertThat(result.size()).isEqualTo(reservationSlots.size());
    }

    @Test
    @DisplayName("실패 - 없는 식당 Id가 주어지면, EntityNotFoundException을 던진다.")
    void findReservationSlotsByDateWithNotExistTest() {
        // Given
        Long restaurantId = 999L;
        LocalDate date = LocalDate.of(2025, 8, 26);
        given(restaurantRepository.existsById(restaurantId)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> reservationSlotService.findReservationSlotsByDate(restaurantId, date))
                .isInstanceOf(EntityNotFoundException.class)
                .hasMessage("해당 식당을 찾을 수 없습니다.");
    }

    private ReservationSlotEntity createResevationSlot(LocalDateTime slotAt) {
        return ReservationSlotEntity.of(null, slotAt, 6, 2);
    }
}
