package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.config.TestSecurityConfig;
import com.rodemtree.yeyakitda.dto.EventBannerDto;
import com.rodemtree.yeyakitda.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EventController.class)
@Import(TestSecurityConfig.class)
@DisplayName("컨트롤러 - 이벤트")
class EventControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EventService eventService;

    @Test
    @DisplayName("성공 - 이벤트 배너 목록을 요청하면 200 OK와 함께 DTO 목록을 반환한다.")
    void getEventBannersTest() throws Exception {
        // Given
        EventBannerDto eventBannerDto1 = createEventBannerDto(1L, "테스트 제목1", "http://test-image1.com");
        EventBannerDto eventBannerDto2 = createEventBannerDto(2L, "테스트 제목2", "http://test-image2.com");
        List<EventBannerDto> eventBannerDtos = List.of(eventBannerDto1, eventBannerDto2);
        given(eventService.findInProgressBanners()).willReturn(eventBannerDtos);

        // When & Then
        mockMvc.perform(get("/api/events/banners"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공적으로 이벤트 배너를 조회했습니다."))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data[0].title").value("테스트 제목1"));

        then(eventService).should().findInProgressBanners();

    }

    private EventBannerDto createEventBannerDto(Long eventId, String title, String bannerImageUrl) {
        return EventBannerDto.builder()
                .eventId(eventId)
                .title(title)
                .bannerImageUrl(bannerImageUrl)
                .build();
    }
}
