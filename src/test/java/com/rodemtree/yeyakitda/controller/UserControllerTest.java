package com.rodemtree.yeyakitda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodemtree.yeyakitda.config.TestSecurityConfig;
import com.rodemtree.yeyakitda.dto.request.SignUpRequestDto;
import com.rodemtree.yeyakitda.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.Mockito.never;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("컨트롤러 - 유저")
@Import({TestSecurityConfig.class, UserControllerTest.TestController.class})
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @Test
    @DisplayName("성공 - 정상적인 회원가입 요청 시, 201 Created 상태 코드를 응답한다.")
    void signUpTest() throws Exception {
        // Given
        SignUpRequestDto dto = createSignUpRequestDto();
        willDoNothing().given(userService).signUp(any());
        int expectedStatus = HttpStatus.CREATED.value();
        String expectedMessage = "성공적으로 회원가입 되었습니다.";

        // When & Then
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto))
                )
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(expectedStatus))
                .andExpect(jsonPath("$.message").value(expectedMessage))
                .andExpect(jsonPath("$.data").doesNotExist());

        then(userService).should().signUp(any());
    }

    @ParameterizedTest
    @MethodSource("invalidSignUpRequests")
    @DisplayName("실패 - 유효하지 않은 데이터로 회원가입을 요청하면, 400 Bad Request를 응답한다.")
    void signUpWithInvalidDataTest(SignUpRequestDto invalidDto) throws Exception {
        // Given


        // When & Then
        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidDto))
        ).andExpect(status().isBadRequest());

        then(userService).should(never()).signUp(any());

    }

    @RestController
    static class TestController {
        @GetMapping("/api/users/me")
        public String getMyInfo() {
            return "This is a secured endpoint for testing.";
        }
    }

    @Test
    @DisplayName("실패 - 인증 없이 보호된 API에 접근하면, 401 Unauthorized를 응답한다.")
    void accessDeniedTest() throws Exception {
        // Given

        // When & Then
        mockMvc.perform(get("/api/users/me")) // 아직 존재하지 않는, 인증이 필요한 가상의 API
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("인증이 필요한 서비스입니다."));
    }

    static Stream<Arguments> invalidSignUpRequests() {
        return Stream.of(
                Arguments.of(new SignUpRequestDto("홍길동", "  ", "pw123!", "닉", "주소", "010-1234-1234")), // 이메일 공백
                Arguments.of(new SignUpRequestDto("홍길동", "invalid-email", "pw123!", "닉", "주소", "010-1234-1234")), // 이메일 형식 오류
                Arguments.of(new SignUpRequestDto(null, "test@test.com", "pw123!", "닉", "주소", "010-1234-1234")), // 이름 null
                Arguments.of(new SignUpRequestDto("", "test@test.com", "pw123!", "닉", "주소", "010-1234-1234")) // 이름 ""
        );
    }

    private SignUpRequestDto createSignUpRequestDto() {
        return SignUpRequestDto.of("김태완", "test@test.com", "test1234!", "rodem", "경기도 구리시", "010-1234-5678");
    }
}
