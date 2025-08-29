package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.request.ReservationRequestDto;
import com.rodemtree.yeyakitda.entity.ReservationEntity;
import com.rodemtree.yeyakitda.entity.ReservationSlotEntity;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.exception.ReservationException;
import com.rodemtree.yeyakitda.repository.ReservationRepository;
import com.rodemtree.yeyakitda.repository.ReservationSlotRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@DisplayName("비즈니스 로직 - 예약")
@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

    @InjectMocks
    private ReservationService reservationService;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationSlotRepository reservationSlotRepository;

    @Test
    @DisplayName("성공 - 예약 정보를 입력하면 예약을 생성하고 슬롯의 예약 인원을 증가시킨다.")
    void createReservationTest() {
        // Given
        String userEmail = "test@test.com";
        Long slotId = 1L;
        int headCount = 2;

        ReservationRequestDto reservationRequestDto = ReservationRequestDto.of(slotId, headCount);
        UserEntity user = createUser(userEmail);
        RestaurantEntity restaurantEntity = RestaurantEntity.builder().build();
        ReflectionTestUtils.setField(restaurantEntity, "id", 1L);
        int reservedCapacity = 0;
        ReservationSlotEntity slot = createReservationSlot(restaurantEntity, reservedCapacity);
        ReservationEntity reservationEntity = createReservation(user, slot);
        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(reservationSlotRepository.findById(slotId)).willReturn(Optional.of(slot));
        given(reservationRepository.save(any())).willReturn(reservationEntity);

        // When
        reservationService.createReservation(userEmail, reservationRequestDto);

        // Then
        then(userRepository).should().findByEmail(userEmail);
        then(reservationSlotRepository).should().findById(slotId);

        ArgumentCaptor<ReservationEntity> reservationCaptor = ArgumentCaptor.forClass(ReservationEntity.class);
        then(reservationRepository).should().save(reservationCaptor.capture());
        ReservationEntity reservation = reservationCaptor.getValue();
        assertThat(reservation.getRestaurant().getId()).isEqualTo(1L);
        assertThat(reservation.getUser().getEmail()).isEqualTo(userEmail);
        assertThat(reservation.getHeadCount()).isEqualTo(headCount);
        
        assertThat(slot.getReservedCapacity()).isEqualTo(reservedCapacity + headCount);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 유저 이메일로 예약을 하면 EntityNotFoundException을 던진다.")
    void createReservationWithNotExistUserTest() {
        // Given
        String notExistEmail = "ttttt@test.com";
        Long slotId = 1L;
        int headCount = 2;

        ReservationRequestDto reservationRequestDto = ReservationRequestDto.of(slotId, headCount);
        given(userRepository.findByEmail(notExistEmail)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(notExistEmail, reservationRequestDto))
                .isInstanceOf(EntityNotFoundException.class);
        then(reservationSlotRepository).should(never()).findById(slotId);
        then(reservationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 예약 슬롯으로 예약을 하면 EntityNotFoundException을 던진다.")
    void createReservationWithNotExistSlotIdTest() {
        // Given
        String userEmail = "test@test.com";
        Long notExistSlotId = 1L;
        int headCount = 2;

        ReservationRequestDto reservationRequestDto = ReservationRequestDto.of(notExistSlotId, headCount);
        UserEntity user = createUser(userEmail);
        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(reservationSlotRepository.findById(notExistSlotId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(userEmail, reservationRequestDto))
                .isInstanceOf(EntityNotFoundException.class);
        then(reservationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패 - 예약자 수 + headCount가 전체 예약가능한 수를 넘으면 ReservationException을 던진다.")
    void createReservationWithGreaterThanPossibleCapcityTest() {
        // Given
        String userEmail = "test@test.com";
        Long slotId = 1L;
        int headCount = 8;

        ReservationRequestDto reservationRequestDto = ReservationRequestDto.of(slotId, headCount);
        UserEntity user = createUser(userEmail);
        RestaurantEntity restaurantEntity = RestaurantEntity.builder().build();
        ReflectionTestUtils.setField(restaurantEntity, "id", 1L);
        int reservedCapacity = 5;
        ReservationSlotEntity slot = createReservationSlot(restaurantEntity, reservedCapacity);
        ReservationEntity reservationEntity = createReservation(user, slot);
        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(reservationSlotRepository.findById(slotId)).willReturn(Optional.of(slot));
        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(userEmail, reservationRequestDto))
                .isInstanceOf(ReservationException.class);

        then(reservationRepository).should(never()).save(any());
    }

    private ReservationSlotEntity createReservationSlot(RestaurantEntity restaurantEntity, int reservedCapacity) {
        return ReservationSlotEntity.of(restaurantEntity, null, 10, reservedCapacity);
    }

    private UserEntity createUser(String userEmail) {
        return UserEntity.builder()
                .email(userEmail)
                .build();
    }

    private ReservationEntity createReservation(UserEntity user, ReservationSlotEntity slot) {
        return ReservationEntity.builder().restaurant(slot.getRestaurant()).user(user).reservationSlot(slot).build();
    }


}
