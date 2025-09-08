package com.rodemtree.yeyakitda.service;

import com.rodemtree.yeyakitda.dto.EventBannerDto;
import com.rodemtree.yeyakitda.repository.EventRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final Clock clock;

    public List<EventBannerDto> findInProgressBanners() {
        LocalDateTime now = LocalDateTime.now(clock);
        return eventRepository.findInProgressBanners(now);
    }
}
