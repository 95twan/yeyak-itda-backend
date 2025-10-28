package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.config.AbstractMySQLContainer;
import com.rodemtree.yeyakitda.config.JpaConfig;
import com.rodemtree.yeyakitda.dto.EventBannerDto;
import com.rodemtree.yeyakitda.entity.EventEntity;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@Import(JpaConfig.class)
@DisplayName("리포지토리 - Event")
class EventRepositoryTest extends AbstractMySQLContainer {

    @Autowired
    private EventRepository eventRepository;


    @Test
    @DisplayName("성공 - 현재 날짜 기준으로 진행 중인 이벤트만 최신순으로 5개 조회한다.")
    void findInProgressBannersTest() {
        // Given
        LocalDateTime now = LocalDateTime.now();

        // 6개의 진행중인 이벤트 저장 (결과가 5개로 잘리는지 확인하기 위함)
        for(int i = 0; i < 5; i++) {
            eventRepository.save(createEvent("진행중인 이벤트 " + i, now.minusDays(1), now.plusDays(1)));
        }
        EventEntity eventEntity = eventRepository.save(createEvent("진행중인 이벤트 " + 5, now.minusDays(1), now.plusDays(1)));
        // 1개의 종료된 이벤트 저장
        eventRepository.save(createEvent("종료된 이벤트", now.minusDays(10), now.minusDays(1)));
        // 1개의 시작 전 이벤트 저장
        eventRepository.save(createEvent("시작 전 이벤트", now.plusDays(1), now.plusDays(10)));

        // When
        List<EventBannerDto> result = eventRepository.findInProgressBanners(now);

        // Then
        assertThat(result).hasSize(5);
        System.out.println(result.get(0).eventId());
        assertThat(result.get(0).eventId()).isEqualTo(eventEntity.getId());
        assertThat(result.get(0).bannerImageUrl()).isEqualTo(eventEntity.getBannerImageUrl());
        assertThat(result.get(0).title()).isEqualTo(eventEntity.getTitle());

    }

    private EventEntity createEvent(String title, LocalDateTime startDate, LocalDateTime endDate) {
        return EventEntity.of(
                title,
                "content",
                "http://banner.url",
                startDate,
                endDate
        );
    }
}
