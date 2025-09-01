package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.ReservationDto;
import com.rodemtree.yeyakitda.dto.UserInfoDto;
import com.rodemtree.yeyakitda.dto.request.SignUpRequestDto;
import com.rodemtree.yeyakitda.entity.*;
import com.rodemtree.yeyakitda.exception.DuplicateException;
import com.rodemtree.yeyakitda.mapper.ReservationMapper;
import com.rodemtree.yeyakitda.mapper.UserMapper;
import com.rodemtree.yeyakitda.repository.ReservationRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@DisplayName("비즈니스 로직 - 유저")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ReservationRepository reservationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private final UserMapper userMapper = Mappers.getMapper(UserMapper.class);
    private final ReservationMapper reservationMapper = Mappers.getMapper(ReservationMapper.class);

    @BeforeEach
    void setUp() {
        userService = new UserService(userRepository, reservationRepository, passwordEncoder, userMapper, reservationMapper);
    }

    @Test
    @DisplayName("정상 - 회원가입에 필요한 정보가 모두 주어지면 새로운 사용자를 생성해 저장한다.")
    void signUpTest() {
        // Given
        SignUpRequestDto dto = createSignUpRequestDto();
        String expectedEncodedPassword = "encoded" + dto.password();
        given(userRepository.existsByEmail(dto.email())).willReturn(false);
        given(userRepository.existsByNickname(dto.nickname())).willReturn(false);
        given(userRepository.existsByPhoneNumber(dto.phoneNumber())).willReturn(false);
        given(passwordEncoder.encode(dto.password())).willReturn(expectedEncodedPassword);

        // When
        userService.signUp(dto);

        // Then
        then(passwordEncoder).should().encode(dto.password());

        ArgumentCaptor<UserEntity> userCaptor = ArgumentCaptor.forClass(UserEntity.class);
        then(userRepository).should().save(userCaptor.capture());

        UserEntity savedUser = userCaptor.getValue();
        assertThat(savedUser.getEmail()).isEqualTo(dto.email());
        assertThat(savedUser.getPassword()).isEqualTo(expectedEncodedPassword);
        assertThat(savedUser.getName()).isEqualTo(dto.name());
        assertThat(savedUser.getNickname()).isEqualTo(dto.nickname());
        assertThat(savedUser.getAddress()).isEqualTo(dto.address());
    }

    @Test
    @DisplayName("실패 - 이미 가입된 이메일로 회원가입을 시도한면 예외를 발생시킨다.")
    void signUpWithDuplicateEmailTest() {
        // Given
        SignUpRequestDto dto = createSignUpRequestDto();
        given(userRepository.existsByEmail(dto.email())).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.signUp(dto))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContaining("이메일");

        then(userRepository).should().existsByEmail(dto.email());
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패 - 이미 가입된 닉네임으로 회원가입을 시도한면 예외를 발생시킨다.")
    void signUpWithDuplicateNicknameTest() {
        // Given
        SignUpRequestDto dto = createSignUpRequestDto();
        given(userRepository.existsByNickname(dto.nickname())).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.signUp(dto))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContaining("닉네임");

        then(userRepository).should().existsByNickname(dto.nickname());
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("실패 - 이미 가입된 전화번호로 회원가입을 시도한면 예외를 발생시킨다.")
    void signUpWithDuplicatePhoneNumberTest() {
        // Given
        SignUpRequestDto dto = createSignUpRequestDto();
        given(userRepository.existsByPhoneNumber(dto.phoneNumber())).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> userService.signUp(dto))
                .isInstanceOf(DuplicateException.class)
                .hasMessageContaining("전화번호");

        then(userRepository).should().existsByPhoneNumber(dto.phoneNumber());
        then(userRepository).should(never()).save(any());
    }

    @Test
    @DisplayName("성공 - 존재하는 이메일로 내 정보를 요청하면, UserInfoDto를 반환한다.")
    void getUserInfoTest() {
        // Given
        String userEmail = "test@test.com";
        UserEntity userEntity = createUserEntity(userEmail);
        given(userRepository.findByEmail(userEmail)).willReturn(Optional.of(userEntity));

        // When
        UserInfoDto result = userService.getUserInfo(userEmail);

        // Then
        then(userRepository).should().findByEmail(userEmail);
        assertThat(result.email()).isEqualTo(userEmail);

    }

    @Test
    @DisplayName("실패 - 존재하지 않는 이메일로 내 정보를 요청하면, EntityNotFoundException 예외를 발생시킨다.")
    void getUserInfoWithNonExistentUserTest() {
        // Given
        String nonExistentEmail = "nonexistent@test.com";
        given(userRepository.findByEmail(nonExistentEmail)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserInfo(nonExistentEmail))
                .isInstanceOf(EntityNotFoundException.class);

        then(userRepository).should().findByEmail(nonExistentEmail);
    }

    @Test
    @DisplayName("성공 - 사용자 이메일로 예약 목록을 조회하면, 해당 사용자의 예약 DTO 목록을 반환한다.")
    void getUserReservationsTest() {
        // Given
        String userEmail = "test@test.com";
        UserEntity userEntity = createUserEntity(userEmail);

        RestaurantEntity restaurant1 = createRestaurant(1L, "김밥천국");
        RestaurantEntity restaurant2 = createRestaurant(2L, "돈까스의 정석");

        ReservationSlotEntity slot1 = createReservationSlot(restaurant1, LocalDateTime.of(2025, 9, 2, 12, 30));
        ReservationSlotEntity slot2 = createReservationSlot(restaurant2, LocalDateTime.of(2025, 9, 5, 19, 0));

        List<ReservationEntity> reservations = List.of(
                createReservation(101L, userEntity, slot1, 2, ReservationStatus.WAITING),
                createReservation(102L, userEntity, slot2, 4, ReservationStatus.RESERVED)
        );

        given(reservationRepository.findByUser_Email(userEmail)).willReturn(reservations);

        // When
        List<ReservationDto> result = userService.getUserReservations(userEmail);

        // Then
        then(reservationRepository).should().findByUser_Email(userEmail);
        assertThat(result).hasSize(2);

        assertThat(result.get(0).reservationId()).isEqualTo(101L);
        assertThat(result.get(0).restaurantName()).isEqualTo("김밥천국");
        assertThat(result.get(0).status()).isEqualTo(ReservationStatus.WAITING.getValue());

        assertThat(result.get(1).reservationId()).isEqualTo(102L);
        assertThat(result.get(1).restaurantName()).isEqualTo("돈까스의 정석");
        assertThat(result.get(1).headCount()).isEqualTo(4);

    }

    private ReservationSlotEntity createReservationSlot(RestaurantEntity restaurant, LocalDateTime time) {
        return ReservationSlotEntity.of(restaurant, time, 10, 0);
    }


    private UserEntity createUserEntity(String userEmail) {
        return UserEntity.builder()
                .email(userEmail)
                .build();
    }

    private RestaurantEntity createRestaurant(Long id, String name) {
        RestaurantEntity restaurant = RestaurantEntity.builder().name(name).build();
        // ReflectionTestUtils를 사용해 private 필드인 id에 값을 설정합니다.
        ReflectionTestUtils.setField(restaurant, "id", id);
        return restaurant;
    }

    private ReservationEntity createReservation(Long id, UserEntity user, ReservationSlotEntity slot, int headCount, ReservationStatus status) {
        ReservationEntity reservation = ReservationEntity.builder()
                .user(user)
                .restaurant(slot.getRestaurant())
                .reservationSlot(slot)
                .headCount(headCount)
                .build();
        ReflectionTestUtils.setField(reservation, "status", status);
        ReflectionTestUtils.setField(reservation, "id", id);
        return reservation;
    }

    private SignUpRequestDto createSignUpRequestDto() {
        return SignUpRequestDto.of("김태완", "test@test.com", "test1234!", "rodem", "경기도 구리시", "010-1234-5678");
    }
}
