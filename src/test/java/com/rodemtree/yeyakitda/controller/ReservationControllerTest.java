package com.rodemtree.yeyakitda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodemtree.yeyakitda.config.TestSecurityConfig;
import com.rodemtree.yeyakitda.dto.request.ReservationRequestDto;
import com.rodemtree.yeyakitda.security.CustomUserDetails;
import com.rodemtree.yeyakitda.service.ReservationService;
import com.rodemtree.yeyakitda.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("컨트롤러 - 예약")
@WebMvcTest(ReservationController.class)
@Import(TestSecurityConfig.class)
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

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
    @DisplayName("성공 - 예약 정보를 받아 예약을 생성하고 201 Created 상태 코드를 응답한다.")
    void createReservationTest() throws Exception {
        // Given
        String userEmail = "test@test.com";
        ReservationRequestDto reservationRequestDto = ReservationRequestDto.of(1L, 1);
        willDoNothing().given(reservationService).createReservation(eq(userEmail), any(ReservationRequestDto.class));

        // When & Then
        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservationRequestDto))
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(HttpStatus.CREATED.value()))
                .andExpect(jsonPath("$.message").isNotEmpty());

        then(reservationService).should().createReservation(eq(userEmail), any(ReservationRequestDto.class));

    }
}
