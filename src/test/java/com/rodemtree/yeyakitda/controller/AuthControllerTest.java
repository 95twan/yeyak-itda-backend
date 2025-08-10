package com.rodemtree.yeyakitda.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodemtree.yeyakitda.dto.request.LoginRequestDto;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("컨트롤러 - 인증")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserRepository userRepository;


    @BeforeEach
    void setUp() {
        // 테스트를 위한 사용자 미리 저장
        UserEntity user = UserEntity.builder()
                .email("test@test.com")
                .password(passwordEncoder.encode("test1234!"))
                .nickname("testuser")
                .phoneNumber("010-1234-5678")
                .address("경기도 구리시")
                .name("홍길동")
                .build();
        user.setDefaultRole();

        userRepository.save(user);
    }

    @Test
    @DisplayName("성공 - 올바른 이메일과 비밀번호로 로그인 요청 시, 토큰을 포함한 200 OK를 응답한다.")
    void loginTest() throws Exception {
        // Given
        LoginRequestDto dto = LoginRequestDto.of("test@test.com", "test1234!");

        // When & Then
        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("실패 - 틀린 비밀번호로 로그인 요청 시, 401 Unauthorized를 응답한다.")
    void loginWithWrongPasswordTest() throws Exception {
        // Given
        LoginRequestDto dto = LoginRequestDto.of("test@test.com", "test5678@");

        // When & Then
        mockMvc.perform(post("/api/auth")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }
}
