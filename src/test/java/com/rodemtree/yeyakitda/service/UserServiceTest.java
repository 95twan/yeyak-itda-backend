package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.request.SignUpRequestDto;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

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
        SignUpRequestDto dto = mockSignUpRequestDto();
        String expectedEncodedPassword = "encoded" + dto.password();
        given(userRepository.findByEmail(dto.email())).willReturn(Optional.empty());
        given(passwordEncoder.encode(dto.password())).willReturn(expectedEncodedPassword);

        // When
        userService.signUp(dto);

        // Then
        then(userRepository).should().findByEmail(dto.email());
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
    @DisplayName("정상 - 존재하는 이메일과 비밀번호가 주어졌을 때 access-token을 발급한다.")
    void loginTest() {
        // Given


        // When


        // Then


    }

    private SignUpRequestDto mockSignUpRequestDto() {
        return SignUpRequestDto.of("김태완", "test@test.com", "test1234!", "rodem", "경기도 구리시");
    }
}
