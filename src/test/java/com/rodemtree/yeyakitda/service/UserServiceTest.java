package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.request.SignUpRequestDto;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.exception.DuplicateException;
import com.rodemtree.yeyakitda.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@DisplayName("비즈니스 로직 - 유저")
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @InjectMocks
    private UserService userService;

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

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


    private SignUpRequestDto createSignUpRequestDto() {
        return SignUpRequestDto.of("김태완", "test@test.com", "test1234!", "rodem", "경기도 구리시", "010-1234-5678");
    }
}
