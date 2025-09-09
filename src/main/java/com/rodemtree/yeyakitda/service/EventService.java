package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.EventBannerDto;
import com.rodemtree.yeyakitda.dto.EventDto;
import com.rodemtree.yeyakitda.entity.EventEntity;
import com.rodemtree.yeyakitda.mapper.EventMapper;
import com.rodemtree.yeyakitda.repository.EventRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final Clock clock;

    public List<EventBannerDto> findInProgressBanners() {
        LocalDateTime now = LocalDateTime.now(clock);
        return eventRepository.findInProgressBanners(now);
    }

    public EventDto findEvent(Long eventId) {
        EventEntity eventEntity = eventRepository.findById(eventId).orElseThrow(() -> new EntityNotFoundException("해당 이벤트를 찾을 수 없습니다."));
        return eventMapper.eventEntityToEventDto(eventEntity);
    }
}
