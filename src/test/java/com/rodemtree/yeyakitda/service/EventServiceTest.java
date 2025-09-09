package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.EventBannerDto;
import com.rodemtree.yeyakitda.dto.EventDto;
import com.rodemtree.yeyakitda.entity.EventEntity;
import com.rodemtree.yeyakitda.mapper.EventMapper;
import com.rodemtree.yeyakitda.repository.EventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.*;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

@DisplayName("비즈니스 로직 - 이벤트")
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    private EventService eventService;

    @Mock
    private EventRepository eventRepository;


    private final EventMapper eventMapper = Mappers.getMapper(EventMapper.class);

    @Mock
    private Clock clock;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, eventMapper, clock);
    }

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

    @Test
    @DisplayName("성공 - 이벤트 Id가 주어지면 이벤트 DTO를 반환 한다.")
    void findEventTest() {
        // Given
        Long eventId = 1L;
        EventEntity eventEntity = createEvent(eventId, "이벤트 제목", "이벤트 내용", LocalDateTime.now().minusDays(1), LocalDateTime.now().plusDays(1));
        given(eventRepository.findById(eventId)).willReturn(Optional.of(eventEntity));

        // When
        EventDto result = eventService.findEvent(eventId);

        // Then
        assertThat(result.id()).isEqualTo(eventEntity.getId());
        assertThat(result.title()).isEqualTo(eventEntity.getTitle());
        assertThat(result.content()).isEqualTo(eventEntity.getContent());
        assertThat(result.startDate()).isEqualTo(eventEntity.getStartDate());
        assertThat(result.endDate()).isEqualTo(eventEntity.getEndDate());
    }

    private EventEntity createEvent(Long eventId, String title, String content, LocalDateTime startDate, LocalDateTime endDate) {
        EventEntity eventEntity = EventEntity.of(title, content,"http://banner.url", startDate, endDate);
        ReflectionTestUtils.setField(eventEntity, "id", eventId);
        return eventEntity;
    }

    private EventBannerDto createEventBannerDto(Long eventId, String title, String bannerImageUrl) {
        return EventBannerDto.builder()
                .eventId(eventId)
                .title(title)
                .bannerImageUrl(bannerImageUrl)
                .build();
    }
}
