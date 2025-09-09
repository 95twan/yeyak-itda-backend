package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.config.TestSecurityConfig;
import com.rodemtree.yeyakitda.dto.EventBannerDto;
import com.rodemtree.yeyakitda.dto.EventDto;
import com.rodemtree.yeyakitda.service.EventService;
import jakarta.persistence.EntityNotFoundException;
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
    @DisplayName("성공 - 이벤트 배너 목록을 요청하면 200 OK와 함께 EventBannerDto 목록을 반환한다.")
    void getInProgressBannersTest() throws Exception {
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

    @Test
    @DisplayName("성공 - 이벤트 id로 요청하면 200 OK와 함께 EventDto를 반환한다.")
    void getEventTest() throws Exception {
        // Given
        Long eventId = 1L;
        EventDto eventDto = createEventDto(eventId, "테스트 제목", "테스트 내용");
        given(eventService.findEvent(eventId)).willReturn(eventDto);

        // When & Then
        mockMvc.perform(get("/api/events/" + eventId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value(200))
                .andExpect(jsonPath("$.message").value("성공적으로 이벤트 상세 정보를 조회했습니다."))
                .andExpect(jsonPath("$.data.id").value(eventId))
                .andExpect(jsonPath("$.data.title").value("테스트 제목"))
                .andExpect(jsonPath("$.data.content").value("테스트 내용"));

        then(eventService).should().findEvent(eventId);

    }

    @Test
    @DisplayName("실패 - 없는 이벤트 id로 요청하면 404 NotFound를 반환한다.")
    void getEventWithNotExistEventIdTest() throws Exception {
        // Given
        Long eventId = 999L;
        given(eventService.findEvent(eventId)).willThrow(new EntityNotFoundException("해당 이벤트를 찾을 수 없습니다."));

        // When & Then
        mockMvc.perform(get("/api/events/" + eventId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").value("해당 이벤트를 찾을 수 없습니다."));

        then(eventService).should().findEvent(eventId);
    }

    private EventBannerDto createEventBannerDto(Long eventId, String title, String bannerImageUrl) {
        return EventBannerDto.builder()
                .eventId(eventId)
                .title(title)
                .bannerImageUrl(bannerImageUrl)
                .build();
    }

    private EventDto createEventDto(Long eventId, String title, String content) {
        return EventDto.builder()
                .id(eventId)
                .title(title)
                .content(content)
                .build();
    }
}
