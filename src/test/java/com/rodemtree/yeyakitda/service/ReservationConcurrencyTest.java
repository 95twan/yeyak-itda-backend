package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.config.AbstractIntegrationContainer;
import com.rodemtree.yeyakitda.dto.request.ReservationRequestDto;
import com.rodemtree.yeyakitda.entity.ReservationSlotEntity;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.exception.ReservationException;
import com.rodemtree.yeyakitda.repository.ReservationRepository;
import com.rodemtree.yeyakitda.repository.ReservationSlotRepository;
import com.rodemtree.yeyakitda.repository.RestaurantRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import org.hibernate.exception.LockAcquisitionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.CannotAcquireLockException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.jpa.JpaSystemException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
@ActiveProfiles("test")
@DisplayName("동시성 테스트 - 예약 서비스")
public class ReservationConcurrencyTest extends AbstractIntegrationContainer {
    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RestaurantRepository restaurantRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ReservationSlotService reservationSlotService;

    @Autowired
    private PlatformTransactionManager transactionManager;

    private Long restaurantId;
    private Long slotId;
    private int totalCapacity;
    @Autowired
    private ReservationSlotRepository reservationSlotRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @BeforeEach
    void setup() {
        UserEntity user = UserEntity.builder()
                .email("test@test.com")
                .password("password")
                .name("Test Owner")
                .nickname("testowner")
                .address("Test Address")
                .phoneNumber("010-0000-0000")
                .build();
        user.setDefaultRole();
        userRepository.save(user);

        RestaurantEntity restaurant = RestaurantEntity.builder()
                .name("테스트 식당")
                .user(user)
                .description("설명")
                .phoneNumber("010-1234-5678")
                .address("주소")
                .category("한식")
                .build();
        restaurantRepository.save(restaurant);
        this.restaurantId = restaurant.getId();

        this.totalCapacity = 10;
        ReservationSlotEntity slot = ReservationSlotEntity.of(restaurant, LocalDateTime.now().plusDays(1L), totalCapacity, 0);
        reservationSlotRepository.save(slot);
        this.slotId = slot.getId();
    }

    @AfterEach
    void cleanup() {
        reservationRepository.deleteAllInBatch();
        reservationSlotRepository.deleteAllInBatch();
        restaurantRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
    }

    @Test
    @DisplayName("성공 - 여러 사용자가 동시에 예약을 시도해도, 예약 가능 인원을 초과하지 않는다.")
    void concurrentReservationTest() throws Exception {
        // Given
        int threadCount = 20;
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(threadCount);

        // When
        for (int i = 0; i < threadCount; i++) {
            executorService.submit(() -> {
                try {
                    reservationService.createReservation("test@test.com", restaurantId, ReservationRequestDto.of(slotId, 1));
                } catch (Exception e) {
                    System.out.println(e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        // Then
        ReservationSlotEntity finalSlot = reservationSlotRepository.findById(slotId).orElseThrow();
        assertThat(finalSlot.getReservedCapacity()).isEqualTo(totalCapacity);
    }

    @Test
    @DisplayName("성공 - 락을 점유한 스레드가 있을 때, 락 대기 시간이 초과되면 예외가 발생한다.")
    void concurrentReservationLockTimeoutTest() throws Exception {
        // Given
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        CountDownLatch latch = new CountDownLatch(1);

        // When
        executorService.submit(() -> {
            TransactionTemplate transactionTemplate = new TransactionTemplate(transactionManager);
            transactionTemplate.execute(status -> {
                try {
                    reservationService.createReservation("test@test.com", restaurantId, new ReservationRequestDto(slotId, 1));
                    System.out.println("스레드 1: 락 획득, 11초 대기 시작");
                    latch.countDown();
                    Thread.sleep(11000);
                } catch (Exception e) {
                    fail("락을 획득해야 하는 스레드에서 예외 발생: " + e.getMessage());
                }
                return null;
            });
        });

        // 스레드 1이 락을 획득할 때까지 대기
        latch.await(2, TimeUnit.SECONDS);

        // 스레드 2: 락 획득을 시도하지만 타임아웃을 기대하는 스레드
        Future<?> future = executorService.submit(() -> {
            reservationService.createReservation("test@test.com", restaurantId, new ReservationRequestDto(slotId, 1));
            return null;
        });

        // 스레드 2 결과에서 예외 확인
        assertThatThrownBy(future::get)
                .hasCauseInstanceOf(ReservationException.class);

        executorService.shutdown();
        executorService.awaitTermination(15, TimeUnit.SECONDS);

        // Then
        ReservationSlotEntity finalSlot = reservationSlotRepository.findById(slotId).orElseThrow();
        assertThat(finalSlot.getReservedCapacity()).isEqualTo(1);
    }
}
