package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.EventBannerDto;
import com.rodemtree.yeyakitda.repository.EventRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.*;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@DisplayName("비즈니스 로직 - 이벤트")
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @InjectMocks
    private EventService eventService;

    @Mock
    private EventRepository eventRepository;

    @Mock
    private Clock clock;

    @Test
    @DisplayName("성공 - 진행 중인 이벤트 배너 목록을 최신순으로 5개 조회한다.")
    void findInProgressBannersTest() {
        // Given
        LocalDateTime fixedNow = LocalDateTime.of(2025, 9, 8, 18, 0, 0);
        ZoneId seoulZone = ZoneId.of("Asia/Seoul");
        Instant fixedInstant = fixedNow.atZone(seoulZone).toInstant();
        given(clock.instant()).willReturn(fixedInstant);
        given(clock.getZone()).willReturn(seoulZone);

        EventBannerDto eventBannerDto1 = createEventBannerDto(1L, "테스트 제목1", "http://test-image1.com");
        EventBannerDto eventBannerDto2 = createEventBannerDto(2L, "테스트 제목2", "http://test-image2.com");
        EventBannerDto eventBannerDto3 = createEventBannerDto(3L, "테스트 제목3", "http://test-image3.com");
        List<EventBannerDto> eventBannerDtos = List.of(eventBannerDto1, eventBannerDto2, eventBannerDto3);
        given(eventRepository.findInProgressBanners(fixedNow)).willReturn(eventBannerDtos);

        // When
        List<EventBannerDto> result = eventService.findInProgressBanners();

        // Then
        assertThat(result).hasSize(eventBannerDtos.size());
    }

    private EventBannerDto createEventBannerDto(Long eventId, String title, String bannerImageUrl) {
        return EventBannerDto.builder()
                .eventId(eventId)
                .title(title)
                .bannerImageUrl(bannerImageUrl)
                .build();
    }
}
