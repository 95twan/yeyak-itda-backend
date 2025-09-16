package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

@DisplayName("비즈니스 로직 - 인증")
@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {

    @InjectMocks
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("성공 - 존재하는 이메일로 사용자를 조회하면, UserDetails 객체를 반환한다.")
    void loadUserByUsernameTest() {
        // Given
        String email = "test@test.com";
        UserEntity user = createUser(email);
        user.setDefaultRole(); // Role 설정
        given(userRepository.findByEmail(email)).willReturn(Optional.of(user));

        // When
        UserDetails userDetails = userDetailsService.loadUserByUsername(email);

        // Then
        assertThat(userDetails.getUsername()).isEqualTo(email);
    }

    @Test
    @DisplayName("실패 - 존재하지 않는 이메일로 조회하면, UsernameNotFoundException 예외를 발생시킨다.")
    void loadUserByUsernameWithNonExistentUserTest() {
        // Given
        String email = "nonexistent@test.com";
        given(userRepository.findByEmail(email)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userDetailsService.loadUserByUsername(email))
                .isInstanceOf(UsernameNotFoundException.class);
    }

    private UserEntity createUser(String email) {
        return UserEntity.builder()
                .email(email)
                .password("encoded-pw")
                .build();
    }
}
