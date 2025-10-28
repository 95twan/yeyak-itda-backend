package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.request.ReservationRequestDto;
import com.rodemtree.yeyakitda.entity.ReservationEntity;
import com.rodemtree.yeyakitda.entity.ReservationSlotEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.exception.ReservationException;
import com.rodemtree.yeyakitda.repository.ReservationRepository;
import com.rodemtree.yeyakitda.repository.ReservationSlotRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.hibernate.exception.LockAcquisitionException;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReservationService {

    private final UserRepository userRepository;
    private final ReservationRepository reservationRepository;
    private final ReservationSlotRepository reservationSlotRepository;
    private final EntityManager entityManager;


    @Transactional
    public void createReservation(String userEmail, Long restaurantId, ReservationRequestDto reservationRequestDto) {
        UserEntity userEntity = userRepository.findByEmail(userEmail).orElseThrow(() -> new EntityNotFoundException("유저를 찾을 수 없습니다."));
        ReservationSlotEntity reservationSlotEntity;
        try {
            entityManager.createNativeQuery("SET SESSION innodb_lock_wait_timeout = 10")
                    .executeUpdate();
            reservationSlotEntity = reservationSlotRepository.findByIdWithPessimisticLock(reservationRequestDto.slotId()).orElseThrow(() -> new EntityNotFoundException("해당하는 예약 슬롯을 찾을 수 없습니다."));
        } catch (PessimisticLockingFailureException e) {
            throw new ReservationException("예약이 마감되었거나 다른 사용자가 선점했습니다. 다른 시간을 선택해주세요.");
        }

        if (!reservationSlotEntity.getRestaurant().getId().equals(restaurantId))
            throw new AccessDeniedException("해당 식당의 예약 슬롯이 아닙니다.");

        if (!reservationSlotEntity.isPossibleToReserve(reservationRequestDto.headCount()))
            throw new ReservationException("예약 가능한 인원을 초과했습니다.");

        reservationSlotEntity.addReservedCapacity(reservationRequestDto.headCount());

        ReservationEntity reservationEntity = ReservationEntity.builder()
                .user(userEntity)
                .restaurant(reservationSlotEntity.getRestaurant())
                .reservationSlot(reservationSlotEntity)
                .headCount(reservationRequestDto.headCount())
                .build();
        reservationEntity.setDefaultStatus();
        reservationRepository.save(reservationEntity);
    }

    @Transactional
    public void cancelReservation(String userEmail, Long restaurantId, Long reservationId) {
        ReservationEntity reservationEntity = reservationRepository.findByIdWithUser(reservationId).orElseThrow(() -> new EntityNotFoundException("해당 예약을 찾을 수 없습니다."));

        if (!reservationEntity.getRestaurant().getId().equals(restaurantId))
            throw new AccessDeniedException("해당 식당의 예약 정보가 아닙니다.");

        String reservationOwnerEmail = reservationEntity.getUser().getEmail();
        if (!reservationOwnerEmail.equals(userEmail)) throw new AccessDeniedException("예약을 취소할 권한이 없습니다.");

        ReservationSlotEntity reservationSlotEntity;
        try {
            reservationSlotEntity = reservationSlotRepository.findByIdWithPessimisticLock(reservationEntity.getReservationSlot().getId())
                    .orElseThrow(() -> new EntityNotFoundException("해당하는 예약 슬롯을 찾을 수 없습니다."));

        } catch (PessimisticLockingFailureException e) {
            throw new ReservationException("예약 취소에 실패했습니다. 잠시 후 다시 시도해 주세요.");
        }

        reservationSlotEntity.removeReservedCapacity(reservationEntity.getHeadCount());
        reservationEntity.cancel();
    }
}
