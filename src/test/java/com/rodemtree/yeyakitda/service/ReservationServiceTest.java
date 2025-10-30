package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.request.ReservationRequestDto;
import com.rodemtree.yeyakitda.entity.*;
import com.rodemtree.yeyakitda.exception.ReservationException;
import com.rodemtree.yeyakitda.repository.ReservationRepository;
import com.rodemtree.yeyakitda.repository.ReservationSlotRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.persistence.Query;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
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

    @Mock
    private Query query;

    @Test
    @DisplayName("성공 - 예약 정보를 입력하면 예약을 생성하고 슬롯의 예약 인원을 증가시킨다.")
    void createReservationTest() {
        // Given
        String userEmail = "test@test.com";
        Long restaurantId = 1L;
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
        given(reservationSlotRepository.findByIdWithPessimisticLock(slotId)).willReturn(Optional.of(slot));
        given(reservationRepository.save(any())).willReturn(reservationEntity);

        // When
        reservationService.createReservation(userEmail, restaurantId, reservationRequestDto);

        // Then
        then(userRepository).should().findByEmail(userEmail);
        then(reservationSlotRepository).should().findByIdWithPessimisticLock(slotId);

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
        Long restaurantId = 1L;
        Long slotId = 1L;
        int headCount = 2;

        ReservationRequestDto reservationRequestDto = ReservationRequestDto.of(slotId, headCount);
        given(userRepository.findByEmail(notExistEmail)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(notExistEmail, restaurantId, reservationRequestDto))
                .isInstanceOf(EntityNotFoundException.class);
        then(reservationSlotRepository).should(never()).findById(slotId);
        then(reservationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 예약 슬롯으로 예약을 하면 EntityNotFoundException을 던진다.")
    void createReservationWithNotExistSlotIdTest() {
        // Given
        String userEmail = "test@test.com";
        Long restaurantId = 1L;
        Long slotId = 1L;
        int headCount = 2;

        ReservationRequestDto reservationRequestDto = ReservationRequestDto.of(slotId, headCount);
        UserEntity user = createUser(userEmail);
        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(reservationSlotRepository.findByIdWithPessimisticLock(slotId))
                .willThrow(new PessimisticLockingFailureException("락 획득 실패"));


        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(userEmail, restaurantId, reservationRequestDto))
                .isInstanceOf(ReservationException.class);
        then(reservationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패 - 예약 슬롯 락 획득을 실패하면 ReservationException을 던진다.")
    void createReservationWhenPessimisticLockingFailureOccursTest() {
        // Given
        String userEmail = "test@test.com";
        Long restaurantId = 1L;
        Long notExistSlotId = 1L;
        int headCount = 2;

        ReservationRequestDto reservationRequestDto = ReservationRequestDto.of(notExistSlotId, headCount);
        UserEntity user = createUser(userEmail);
        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(reservationSlotRepository.findByIdWithPessimisticLock(notExistSlotId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(userEmail, restaurantId, reservationRequestDto))
                .isInstanceOf(EntityNotFoundException.class);
        then(reservationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패 - 다른 식당의 예약 슬롯으로 예약을 생성하면 AccessDeniedException을 던진다.")
    void createReservationWithMismatchedRestaurantIdTest() {
        // Given
        String userEmail = "test@test.com";
        Long requestedRestaurantId = 999L; // URL로 요청된 식당 ID
        Long actualSlotRestaurantId = 1L;   // 실제 슬롯이 속한 식당 ID

        ReservationRequestDto reservationRequestDto = ReservationRequestDto.of(1L, 2);
        UserEntity user = createUser(userEmail);
        RestaurantEntity restaurant = createRestaurant(actualSlotRestaurantId, "실제 식당");
        ReservationSlotEntity slot = createReservationSlot(restaurant, 10, 5);

        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(user));
        given(reservationSlotRepository.findByIdWithPessimisticLock(reservationRequestDto.slotId())).willReturn(Optional.of(slot));

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(userEmail, requestedRestaurantId, reservationRequestDto))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 식당의 예약 슬롯이 아닙니다.");

        then(reservationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패 - 예약자 수 + headCount가 전체 예약가능한 수를 넘으면 ReservationException을 던진다.")
    void createReservationWithGreaterThanPossibleCapcityTest() {
        // Given
        String userEmail = "test@test.com";
        Long restaurantId = 1L;
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
        given(reservationSlotRepository.findByIdWithPessimisticLock(slotId)).willReturn(Optional.of(slot));

        // When & Then
        assertThatThrownBy(() -> reservationService.createReservation(userEmail, restaurantId, reservationRequestDto))
                .isInstanceOf(ReservationException.class);

        then(reservationRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("성공 - 자신의 예약을 취소하면, 예약 상태가 CANCELLED로 변경되고 슬롯의 예약된 인원이 감소한다.")
    void cancelReservationTest() {
        // Given
        String userEmail = "test@test.com";
        Long restaurantId = 1L;
        Long reservationId = 1L;
        int headCount = 2;
        int initialReservedCapacity = 5;
        
        UserEntity user = createUser(userEmail);
        RestaurantEntity restaurant = createRestaurant(1L, "테스트 식당");
        ReservationSlotEntity slot = createReservationSlot(restaurant, 10, initialReservedCapacity);

        ReservationEntity reservation = createReservation(reservationId, user, slot, headCount, ReservationStatus.RESERVED);

        given(reservationRepository.findByIdWithUser(reservationId)).willReturn(Optional.of(reservation));
        given(reservationSlotRepository.findByIdWithPessimisticLock(slot.getId())).willReturn(Optional.of(slot));

        // When
        reservationService.cancelReservation(userEmail, restaurantId, reservationId);

        // Then
        then(reservationRepository).should().findByIdWithUser(reservationId);
        then(reservationSlotRepository).should().findByIdWithPessimisticLock(slot.getId());

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(slot.getReservedCapacity()).isEqualTo(initialReservedCapacity - headCount);
    }

    @Test
    @DisplayName("실패 - 다른 식당 ID로 예약을 취소하려고 하면, AccessDeniedException 예외가 발생한다.")
    void cancelReservationWithMismatchedRestaurantIdTest() {
        // Given
        String userEmail = "test@test.com";
        Long requestedRestaurantId = 999L; // URL로 요청된 식당 ID
        Long actualReservationRestaurantId = 1L;   // 실제 예약이 속한 식당 ID
        Long reservationId = 1L;

        UserEntity user = createUser(userEmail);
        RestaurantEntity restaurant = createRestaurant(actualReservationRestaurantId, "실제 식당");
        ReservationSlotEntity slot = createReservationSlot(restaurant, 10, 5);
        ReservationEntity reservation = createReservation(reservationId, user, slot, 2, ReservationStatus.RESERVED);


        given(reservationRepository.findByIdWithUser(reservationId)).willReturn(Optional.of(reservation));

        // When & Then
        assertThatThrownBy(() -> reservationService.cancelReservation(userEmail, requestedRestaurantId, reservationId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("해당 식당의 예약 정보가 아닙니다.");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("실패 - 다른 사용자의 예약을 취소하려고 하면, AccessDeniedException 예외가 발생한다.")
    void cancelReservationWithNoPermissionTest() {
        // Given
        String userEmail = "test@test.com";
        Long restaurantId = 1L;
        String otherUserEmail = "other@test.com";
        Long reservationId = 1L;

        UserEntity user = createUser(userEmail);
        RestaurantEntity restaurant = createRestaurant(restaurantId, "실제 식당");
        ReservationSlotEntity slot = createReservationSlot(restaurant, 10, 5);
        ReservationEntity reservation = createReservation(reservationId, user, slot, 2, ReservationStatus.RESERVED);

        given(reservationRepository.findByIdWithUser(reservationId)).willReturn(Optional.of(reservation));

        // When & Then
        assertThatThrownBy(() -> reservationService.cancelReservation(otherUserEmail, restaurantId, reservationId))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessage("예약을 취소할 권한이 없습니다.");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.RESERVED);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 예약 ID로 취소를 시도하면, EntityNotFoundException 예외가 발생한다.")
    void cancelReservationWithNonExistentIdTest() {
        // Given
        String userEmail = "test@test.com";
        Long restaurantId = 1L;
        Long nonExistentReservationId = 999L;

        given(reservationRepository.findByIdWithUser(nonExistentReservationId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reservationService.cancelReservation(userEmail, restaurantId, nonExistentReservationId))
                .isInstanceOf(EntityNotFoundException.class);
    }

    private RestaurantEntity createRestaurant(Long id, String name) {
        RestaurantEntity restaurant = RestaurantEntity.builder().name(name).build();
        ReflectionTestUtils.setField(restaurant, "id", id);
        return restaurant;
    }

    private ReservationSlotEntity createReservationSlot(RestaurantEntity restaurantEntity, int reservedCapacity) {
        return createReservationSlot(restaurantEntity, 8, reservedCapacity);
    }

    private ReservationSlotEntity createReservationSlot(RestaurantEntity restaurantEntity, int totalCapacity, int reservedCapacity) {
        ReservationSlotEntity reservationSlot = ReservationSlotEntity.of(restaurantEntity, null, totalCapacity, reservedCapacity);
        ReflectionTestUtils.setField(reservationSlot, "id", 1L);
        return reservationSlot;
    }

    private UserEntity createUser(String userEmail) {
        return UserEntity.builder()
                .email(userEmail)
                .build();
    }

    private ReservationEntity createReservation(UserEntity user, ReservationSlotEntity slot) {
        return createReservation(1L, user, slot, 2, ReservationStatus.RESERVED);
    }

    private ReservationEntity createReservation(Long reservationId, UserEntity user, ReservationSlotEntity slot, int headCount, ReservationStatus reservationStatus) {
        ReservationEntity reservation = ReservationEntity.builder()
                .user(user)
                .restaurant(slot.getRestaurant())
                .reservationSlot(slot)
                .headCount(headCount)
                .build();
        ReflectionTestUtils.setField(reservation, "id", reservationId);
        ReflectionTestUtils.setField(reservation, "status", reservationStatus);
        return reservation;
    }

}
