package com.rodemtree.yeyakitda.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.rodemtree.yeyakitda.dto.request.LoginRequestDto;
import com.rodemtree.yeyakitda.entity.RefreshTokenEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.repository.RefreshTokenRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("컨트롤러 - 인증")
@SpringBootTest
@AutoConfigureMockMvc
@Import(AuthControllerTest.TestController.class)
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

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;


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
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists());
    }

    @Test
    @DisplayName("실패 - 가입되지 않은 이메일로 로그인 요청 시, 401 Unauthorized를 응답한다.")
    void loginWithUnregisteredEmailTest() throws Exception {
        // Given
        LoginRequestDto dto = LoginRequestDto.of("tttt@test.com", "test1234!");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("실패 - 틀린 비밀번호로 로그인 요청 시, 401 Unauthorized를 응답한다.")
    void loginWithWrongPasswordTest() throws Exception {
        // Given
        LoginRequestDto dto = LoginRequestDto.of("test@test.com", "test5678@");

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("성공 - 로그인 요청 시, 저장된 Refresh Token이 없다면 새 Refresh Token을 저장된다.")
    @Transactional
    void loginSavesRefreshToken() throws Exception {
        // Given
        LoginRequestDto dto = LoginRequestDto.of("test@test.com", "test1234!");
        UserEntity user = userRepository.findByEmail(dto.email()).orElseThrow();

        // When & Then
        assertThat(refreshTokenRepository.findByUser_Email(user.getEmail())).isEmpty();

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk());

        assertThat(refreshTokenRepository.findByUser_Email(user.getEmail())).isNotEmpty();
    }

    @Test
    @DisplayName("성공 - 로그인 요청 시, 저장된 Refresh Token이 있다면 새 Refresh Token으로 업데이트한다.")
    @Transactional
    void loginUpdateRefreshToken() throws Exception {
        // Given
        LoginRequestDto dto = LoginRequestDto.of("test@test.com", "test1234!");
        UserEntity user = userRepository.findByEmail(dto.email()).orElseThrow();
        String refreshToken = "refreshToken";
        RefreshTokenEntity refreshTokenEntity = RefreshTokenEntity.builder().token(refreshToken).expireAt(LocalDateTime.now()).user(user).build();
        refreshTokenRepository.save(refreshTokenEntity);

        // When & Then
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isOk());

        RefreshTokenEntity updatedRefreshToken = refreshTokenRepository.findByUser_Email(user.getEmail()).orElseThrow();
        assertThat(updatedRefreshToken.getToken()).isNotEqualTo(refreshToken);
    }

    @RestController
    static class TestController {
        @GetMapping("/api/users/me")
        public String getMyInfo() {
            return "This is a secured endpoint for testing.";
        }
    }

    @Test
    @DisplayName("성공 - 인증이 필요한 api 호출시, 유효한 token을 헤더에 담아 요청하면 200 OK 를 응답한다.")
    void accessWithTokenTest() throws Exception {
        // Given
        LoginRequestDto dto = LoginRequestDto.of("test@test.com", "test1234!");
        String responseBody = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn().getResponse().getContentAsString();
        String accessToken = JsonPath.read(responseBody, "$.data.accessToken");

        // When & Then
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                )
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("실패 - 인증이 필요한 api 호출시, 유효하지 않은 token을 헤더에 담아 요청하면 401 Unauthorized를 응답한다.")
    void accessWithInvalidTokenTest() throws Exception {
        // Given
        String invalidAccessToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.invalid-token-payload.invalid-signature";

        // When & Then
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + invalidAccessToken)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("실패 - 인증이 필요한 api 호출시, token없이 요청하면 401 Unauthorized를 응답한다.")
    void accessWithoutTokenTest() throws Exception {
        // Given

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("실패 - 인증이 필요한 api 호출시, refreshToken을 헤더에 담아 요청하면 401 Unauthorized를 응답한다.")
    void accessWithRefreshTokenTest() throws Exception {
        // Given
        LoginRequestDto dto = LoginRequestDto.of("test@test.com", "test1234!");
        String responseBody = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn().getResponse().getContentAsString();
        String refreshToken = JsonPath.read(responseBody, "$.data.refreshToken");
        // When & Then
        mockMvc.perform(get("/api/users/me")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("성공 - 유효한 토큰으로 로그아웃 요청 시, 200 OK와 함께 저장된 Refresh Token을 삭제한다.")
    void logoutTest() throws Exception {
        // Given
        LoginRequestDto dto = LoginRequestDto.of("test@test.com", "test1234!");
        String responseBody = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn().getResponse().getContentAsString();
        String accessToken = JsonPath.read(responseBody, "$.data.accessToken");

        // When & Then
        assertThat(refreshTokenRepository.findByUser_Email(dto.email())).isPresent();
        mockMvc.perform(delete("/api/auth/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공적으로 로그아웃 되었습니다."));

        assertThat(refreshTokenRepository.findByUser_Email(dto.email())).isEmpty();

    }

    @Test
    @DisplayName("성공 - 유효한 Refresh Token으로 요청 시, 새로운 Access Token과 Refresh Token을 재발급한다.")
    void reissueTokenTest() throws Exception {
        // Given
        LoginRequestDto dto = LoginRequestDto.of("test@test.com", "test1234!");
        String responseBody = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn().getResponse().getContentAsString();
        String refreshToken = JsonPath.read(responseBody, "$.data.refreshToken");

        // When & Then
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty());
    }
}
