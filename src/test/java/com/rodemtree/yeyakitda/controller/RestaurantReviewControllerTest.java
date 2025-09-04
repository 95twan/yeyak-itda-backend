package com.rodemtree.yeyakitda.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rodemtree.yeyakitda.config.TestSecurityConfig;
import com.rodemtree.yeyakitda.dto.request.ReviewRequestDto;
import com.rodemtree.yeyakitda.security.CustomUserDetails;
import com.rodemtree.yeyakitda.service.ReviewService;
import com.rodemtree.yeyakitda.service.UserDetailsServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.TestExecutionEvent;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.charset.StandardCharsets;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@DisplayName("컨트롤러 - 식당 리뷰")
@WebMvcTest(RestaurantReviewController.class)
@Import(TestSecurityConfig.class)
class RestaurantReviewControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReviewService reviewService;

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
    @DisplayName("성공 - 인증된 사용자가 유효한 리뷰 정보로 요청 시, 201 Created를 응답한다.")
    void createReviewTest() throws Exception {
        // Given
        Long restaurantId = 1L;
        String userEmail = "test@test.com";
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(4, "test comment");

        MockMultipartFile imageFile = new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());
        MockMultipartFile reviewDataFile = new MockMultipartFile("reviewData", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(reviewRequestDto).getBytes(StandardCharsets.UTF_8));

        willDoNothing().given(reviewService).createReview(anyLong(), any(), any(ReviewRequestDto.class), any());

        // When & Then
        mockMvc.perform(multipart(HttpMethod.POST, "/api/restaurants/" + restaurantId + "/reviews")
                        .file(imageFile)
                        .file(reviewDataFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.message").value("성공적으로 리뷰가 등록되었습니다."));

        then(reviewService).should().createReview(eq(restaurantId), eq(userEmail), any(ReviewRequestDto.class), any());
    }

    @Test
    @WithUserDetails(value = "test@test.com", setupBefore = TestExecutionEvent.TEST_EXECUTION, userDetailsServiceBeanName = "userDetailsServiceImpl")
    @DisplayName("실패 - 유효하지 않은 리뷰 정보로 요청 시, 400 Bad Request를 응답한다.")
    void createReviewWithInvalidReviewDataTest() throws Exception {
        // Given
        Long restaurantId = 1L;
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(4, "짧");

        MockMultipartFile imageFile = new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());
        MockMultipartFile reviewDataFile = new MockMultipartFile("reviewData", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(reviewRequestDto).getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipart(HttpMethod.POST, "/api/restaurants/" + restaurantId + "/reviews")
                .file(imageFile)
                .file(reviewDataFile)
                .accept(MediaType.APPLICATION_JSON)
                .with(csrf()))
                .andExpect(status().isBadRequest());

        then(reviewService).should(never()).createReview(anyLong(), any(), any(ReviewRequestDto.class), any());
    }

    @Test
    @DisplayName("실패 - 인증되지 않은 사용자가 리뷰 작성 요청 시, 401 Unauthorized를 응답한다.")
    void createReviewWithoutAuthTest() throws Exception {
        // Given
        Long restaurantId = 1L;
        ReviewRequestDto reviewRequestDto = new ReviewRequestDto(4, "짧");

        MockMultipartFile imageFile = new MockMultipartFile("images", "test.jpg", MediaType.IMAGE_JPEG_VALUE, "test image".getBytes());
        MockMultipartFile reviewDataFile = new MockMultipartFile("reviewData", "", MediaType.APPLICATION_JSON_VALUE, objectMapper.writeValueAsString(reviewRequestDto).getBytes(StandardCharsets.UTF_8));

        // When & Then
        mockMvc.perform(multipart(HttpMethod.POST, "/api/restaurants/" + restaurantId + "/reviews")
                        .file(imageFile)
                        .file(reviewDataFile)
                        .accept(MediaType.APPLICATION_JSON)
                        .with(csrf()))
                .andExpect(status().isUnauthorized());

        then(reviewService).should(never()).createReview(anyLong(), any(), any(ReviewRequestDto.class), any());
    }

}
