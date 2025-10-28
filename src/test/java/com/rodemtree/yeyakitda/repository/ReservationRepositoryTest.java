package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.config.AbstractMySQLContainer;
import com.rodemtree.yeyakitda.config.JpaConfig;
import com.rodemtree.yeyakitda.entity.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@DisplayName("리포지토리 - Reservation")
class ReservationRepositoryTest extends AbstractMySQLContainer {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    private UserEntity user;
    private RestaurantEntity restaurant;
    private ReservationSlotEntity slot;

    @BeforeEach
    void setUp() {
        user = createUser("test@test.com");
        userRepository.save(user);

        restaurant = createRestaurant(user, "테스트 식당");
        restaurantRepository.save(restaurant);

        slot = createReservationSlot(restaurant, LocalDateTime.now().plusDays(1));
        reservationSlotRepository.save(slot);
    }

    @Test
    @DisplayName("성공 - 사용자 이메일로 예약 목록 조회 시, 연관된 식당과 슬롯 정보도 함께 조회한다.")
    void findByUser_EmailWithDetailTest() {
        // Given
        ReservationEntity reservation1 = createReservation(user, slot, 2, ReservationStatus.RESERVED);
        ReservationEntity reservation2 = createReservation(user, slot, 3, ReservationStatus.WAITING);
        reservationRepository.saveAll(List.of(reservation1, reservation2));

        // When
        List<ReservationEntity> result = reservationRepository.findByUser_EmailWithDetail(user.getEmail());

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getRestaurant()).isNotNull();
        assertThat(result.get(0).getRestaurant().getName()).isEqualTo(restaurant.getName());
        assertThat(result.get(0).getReservationSlot()).isNotNull();
        assertThat(result.get(0).getReservationSlot().getSlotAt()).isEqualTo(slot.getSlotAt());

    }

    @Test
    @DisplayName("실패 - 존재하지 않는 사용자 이메일로 조회 시, 빈 리스트를 반환한다.")
    void findByUser_EmailWithDetail_NotFoundTest() {
        // Given
        String nonExistentEmail = "notfound@test.com";

        // When
        List<ReservationEntity> result = reservationRepository.findByUser_EmailWithDetail(nonExistentEmail);

        // Then
        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("성공 - 예약 ID로 예약 조회 시, 연관된 사용자 정보도 함께 조회한다.")
    void findByIdWithUserTest() {
        // Given
        ReservationEntity reservation = createReservation(user, slot, 2, ReservationStatus.RESERVED);
        ReservationEntity savedReservation = reservationRepository.save(reservation);

        // When
        Optional<ReservationEntity> result = reservationRepository.findByIdWithUser(savedReservation.getId());

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getUser()).isNotNull();
        assertThat(result.get().getUser().getEmail()).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 예약 ID로 조회 시, 빈 Optional을 반환한다.")
    void findByIdWithUser_NotFoundTest() {
        // Given
        Long nonExistentId = 999L;

        // When
        Optional<ReservationEntity> result = reservationRepository.findByIdWithUser(nonExistentId);

        // Then
        assertThat(result).isNotPresent();
    }

    private UserEntity createUser(String email) {
        UserEntity userEntity = UserEntity.builder()
                .email(email)
                .name("홍길동")
                .address("경기도 구리시")
                .password("encoded-pw")
                .phoneNumber("010-1234-1234")
                .nickname("testuser")
                .build();
        userEntity.setDefaultRole();
        return userEntity;
    }


    private RestaurantEntity createRestaurant(UserEntity user, String name) {
        return RestaurantEntity.builder()
                .user(user)
                .name(name)
                .description("설명")
                .phoneNumber("02-123-4567")
                .address("서울시")
                .category("한식")
                .build();
    }


    private ReservationSlotEntity createReservationSlot(RestaurantEntity restaurant, LocalDateTime slotAt) {
        return ReservationSlotEntity.of(restaurant, slotAt, 10, 0);
    }


    private ReservationEntity createReservation(UserEntity user, ReservationSlotEntity slot, int headCount, ReservationStatus status) {
        ReservationEntity reservation = ReservationEntity.builder()
                .user(user)
                .restaurant(slot.getRestaurant())
                .reservationSlot(slot)
                .headCount(headCount)
                .build();
        ReflectionTestUtils.setField(reservation, "status", status);
        return reservation;
    }
}
