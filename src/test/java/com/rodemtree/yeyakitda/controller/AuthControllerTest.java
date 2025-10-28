package com.rodemtree.yeyakitda.controller;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.rodemtree.yeyakitda.config.AbstractIntegrationContainer;
import com.rodemtree.yeyakitda.dto.request.LoginRequestDto;
import com.rodemtree.yeyakitda.dto.request.SignUpRequestDto;
import com.rodemtree.yeyakitda.dto.response.ResponseErrorCode;
import com.rodemtree.yeyakitda.entity.RefreshTokenEntity;
import com.rodemtree.yeyakitda.entity.UserEntity;
import com.rodemtree.yeyakitda.exception.DuplicateException;
import com.rodemtree.yeyakitda.repository.RefreshTokenRepository;
import com.rodemtree.yeyakitda.repository.UserRepository;
import com.rodemtree.yeyakitda.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("컨트롤러 - 인증")
@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@Import({AuthControllerTest.TestController.class})
@Transactional
class AuthControllerTest extends AbstractIntegrationContainer {

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

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @MockitoBean
    private Clock clock;

    @MockitoBean
    private UserService userService;


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

        given(clock.instant()).willReturn(Instant.now());
    }

    @AfterEach
    void tearDown() {
        redisTemplate.getConnectionFactory().getConnection().flushAll();
    }

    @Test
    @DisplayName("성공 - 정상적인 회원가입 요청 시, 201 Created 상태 코드를 응답한다.")
    void signUpTest() throws Exception {
        // Given
        SignUpRequestDto dto = createSignUpRequestDto();
        willDoNothing().given(userService).signUp(any());
        int expectedStatus = HttpStatus.CREATED.value();
        String expectedMessage = "성공적으로 회원가입 되었습니다.";

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(userService).should().signUp(any());
    }

    @Test
    @DisplayName("실패 - 중복된 이메일, 닉네임, 전화번호로 회원가입 요청 시, 409 Conflict 상태 코드를 응답한다.")
    void signUpWithDuplicateFieldsTest() throws Exception {
        // Given
        SignUpRequestDto dto = createSignUpRequestDto();
        List<DuplicateException.Field> fields = List.of(DuplicateException.Field.EMAIL, DuplicateException.Field.NICKNAME, DuplicateException.Field.PHONE_NUMBER);
        willThrow(new DuplicateException(fields)).given(userService).signUp(any());

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(HttpStatus.CONFLICT.value()))
                .andExpect(jsonPath("$.message").value(containsString(DuplicateException.Field.EMAIL.getDescription())))
                .andExpect(jsonPath("$.message").value(containsString(DuplicateException.Field.PHONE_NUMBER.getDescription())))
                .andExpect(jsonPath("$.message").value(containsString(DuplicateException.Field.NICKNAME.getDescription())));

        then(userService).should().signUp(any());
    }

    @ParameterizedTest
    @MethodSource("invalidSignUpRequests")
    @DisplayName("실패 - 유효하지 않은 데이터로 회원가입을 요청하면, 400 Bad Request를 응답한다.")
    void signUpWithInvalidDataTest(SignUpRequestDto invalidDto) throws Exception {
        // Given

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto))
        ).andExpect(status().isBadRequest());

        then(userService).should(never()).signUp(any());

    }

    static Stream<Arguments> invalidSignUpRequests() {
        return Stream.of(
                Arguments.of(new SignUpRequestDto("홍길동", "  ", "pw123!", "닉", "주소", "010-1234-1234")), // 이메일 공백
                Arguments.of(new SignUpRequestDto("홍길동", "invalid-email", "pw123!", "닉", "주소", "010-1234-1234")), // 이메일 형식 오류
                Arguments.of(new SignUpRequestDto(null, "test@test.com", "pw123!", "닉", "주소", "010-1234-1234")), // 이름 null
                Arguments.of(new SignUpRequestDto("", "test@test.com", "pw123!", "닉", "주소", "010-1234-1234")) // 이름 ""
        );
    }

    @Test
    @DisplayName("실패 - 비어있는 이름으로 회원가입을 요청하면, 400 Bad Request와 에러 메시지를 응답한다.")
    void signUpWithBlankNameTest() throws Exception {
        // Given
        // 이름만 비어있는 DTO 생성
        SignUpRequestDto dto = new SignUpRequestDto("", "test@test.com", "pw123!", "닉네임", "주소", "010-1234-1234");

        // When & Then
        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isBadRequest()) // 1. HTTP 상태 코드가 400인지 확인
                .andExpect(jsonPath("$.status").value(HttpStatus.BAD_REQUEST.value())) // 2. 응답 DTO의 status 필드 확인
                .andExpect(jsonPath("$.message").value(containsString("name : 이름은 필수 입력 항목입니다."))); // 3. 우리가 만들 "규칙"에 맞는 메시지가 오는지 확인

        then(userService).should(never()).signUp(any());
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
        @GetMapping("/api/auth/test")
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
        mockMvc.perform(get("/api/auth/test")
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
        mockMvc.perform(get("/api/auth/test")
                        .header("Authorization", "Bearer " + invalidAccessToken)
                )
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("실패 - 인증이 필요한 api 호출시, token없이 요청하면 401 Unauthorized를 응답한다.")
    void accessWithoutTokenTest() throws Exception {
        // Given

        // When & Then
        mockMvc.perform(get("/api/auth/test"))
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
        mockMvc.perform(get("/api/auth/test")
                        .header("Authorization", "Bearer " + refreshToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("실패 - 로그아웃 처리된 토큰으로 API 요청 시 401 에러를 응답한다.")
    void accessWithBlacklistedAcessTokenTest() throws Exception {
        // Given
        LoginRequestDto dto = LoginRequestDto.of("test@test.com", "test1234!");
        String responseBody = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn().getResponse().getContentAsString();
        String blacklistedToken = JsonPath.read(responseBody, "$.data.accessToken");
        String key = "blacklist:" + blacklistedToken;
        redisTemplate.opsForValue().set(key, "logout", 60, TimeUnit.SECONDS);

        // When & Then
        mockMvc.perform(get("/api/auth/test")
                        .header("Authorization", "Bearer " + blacklistedToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("성공 - 유효한 토큰으로 로그아웃 요청 시, 200 OK와 함께 AccessToken을 블랙리스트에 저장하고 저장된 Refresh Token을 삭제한다.")
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

        String key = "blacklist:" + accessToken;
        assertThat(redisTemplate.hasKey(key)).isTrue();
        assertThat(redisTemplate.opsForValue().get(key)).isEqualTo("logout");
        assertThat(redisTemplate.getExpire(key, TimeUnit.SECONDS)).isGreaterThan(0L);
    }

    @Test
    @DisplayName("성공 - 유효한 Refresh Token으로 요청 시, 새로운 Access Token과 Refresh Token을 재발급한다.")
    void reissueTokenTest() throws Exception {
        // Given
        Instant loginTime = Instant.now();
        given(clock.instant()).willReturn(loginTime);
        LoginRequestDto dto = LoginRequestDto.of("test@test.com", "test1234!");
        String responseBody = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto))
        ).andReturn().getResponse().getContentAsString();
        String refreshToken = JsonPath.read(responseBody, "$.data.refreshToken");

        given(clock.instant()).willReturn(loginTime.plusSeconds(10));

        // When & Then
        String reissueResponseBody = mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").isNotEmpty())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(jsonPath("$.data.refreshToken").isNotEmpty())
                .andReturn().getResponse().getContentAsString();

        String reissuedRefreshToken = JsonPath.read(reissueResponseBody, "$.data.refreshToken");
        assertThat(reissuedRefreshToken).isNotEqualTo(refreshToken);
    }

    @Test
    @DisplayName("실패 - 유효하지 않은 Refresh Token으로 요청 시, 401 Unauthorized을 응답한다.")
    void reissueTokenWithInvalidRefreshTokenTest() throws Exception {
        String refreshToken = "Invalid refresh token";

        // When & Then
        mockMvc.perform(post("/api/auth/reissue")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("refreshToken", refreshToken))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(ResponseErrorCode.INVALID_TOKEN.getStatus()))
                .andExpect(jsonPath("$.message").value(ResponseErrorCode.INVALID_TOKEN.getMessage()))
                .andReturn().getResponse().getContentAsString();

    }

    private SignUpRequestDto createSignUpRequestDto() {
        return SignUpRequestDto.of("김태완", "test@test.com", "test1234!", "rodem", "경기도 구리시", "010-1234-5678");
    }
}
