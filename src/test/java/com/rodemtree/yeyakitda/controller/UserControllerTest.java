package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.config.TestSecurityConfig;
import com.rodemtree.yeyakitda.dto.ReservationDto;
import com.rodemtree.yeyakitda.dto.UserInfoDto;
import com.rodemtree.yeyakitda.entity.ReservationStatus;
import com.rodemtree.yeyakitda.security.CustomUserDetails;
import com.rodemtree.yeyakitda.service.UserDetailsServiceImpl;
import com.rodemtree.yeyakitda.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("컨트롤러 - 유저")
@Import(TestSecurityConfig.class)
@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    @MockitoBean(name = "userDetailsServiceImpl")
    private UserDetailsServiceImpl userDetailsService;

    @BeforeEach
    void setup() {
        String userEmail = "test@test.com";
        CustomUserDetails userDetails = new CustomUserDetails(userEmail, "password", List.of(new SimpleGrantedAuthority("ROLE_USER")));
        given(userDetailsService.loadUserByUsername(userEmail)).willReturn(userDetails);
    }

    @Test
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION, userDetailsServiceBeanName = "userDetailsServiceImpl")
    @DisplayName("성공 - 인증된 사용자가 내 정보를 요청하면, 200 OK와 함께 사용자 정보를 반환한다.")
    void getMyInfoTest() throws Exception {
        // Given
        String userEmail = "test@test.com";
        UserInfoDto userInfoDto = createUserInfoDto(userEmail);
        given(userService.getUserInfo(userEmail)).willReturn(userInfoDto);

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공적으로 사용자 정보를 조회했습니다."))
                .andExpect(jsonPath("$.data.email").value(userEmail));

        then(userService).should().getUserInfo(userEmail);
    }

    @Test
    @DisplayName("실패 - 인증되지 않은 사용자가 내 정보를 요청하면, 401 Unauthorized를 반환한다.")
    void getMyInfoWithoutAuthTest() throws Exception {
        // Given

        // When & Then
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION, userDetailsServiceBeanName = "userDetailsServiceImpl")
    @DisplayName("성공 - 인증된 사용자가 내 예약 목록을 요청하면, 200 OK와 함께 예약 목록을 반환한다.")
    void getMyReservationsTest() throws Exception {
        // Given
        String userEmail = "test@test.com";
        List<ReservationDto> reservationDtos = List.of(
                createReservationDto("김밥천국"),
                createReservationDto("돈까스의 정석")
        );

        given(userService.getUserReservations(userEmail)).willReturn(reservationDtos);

        // When & Then
        mockMvc.perform(get("/api/users/me/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공적으로 사용자 예약 정보를 조회했습니다."))
                .andExpect(jsonPath("$.data").isArray()) // 응답 데이터가 배열인지 확인
                .andExpect(jsonPath("$.data.length()").value(2)) // 배열의 크기가 2인지 확인
                .andExpect(jsonPath("$.data[0].restaurantName").value("김밥천국"));

        then(userService).should().getUserReservations(userEmail);
    }

    @Test
    @DisplayName("실패 - 인증되지 않은 사용자가 내 예약 정보를 요청하면, 401 Unauthorized를 반환한다.")
    void getMyReservationsWithoutAuthTest() throws Exception {
        // Given

        // When & Then
        mockMvc.perform(get("/api/users/me/reservations"))
                .andExpect(status().isUnauthorized());
    }

    private UserInfoDto createUserInfoDto(String userEmail) {
        return UserInfoDto.builder()
                .email(userEmail)
                .build();
    }

    private ReservationDto createReservationDto(String restaurantName) {
        return ReservationDto.builder()
                .restaurantName(restaurantName)
                .build();
    }

}
