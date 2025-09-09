package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.EventBannerDto;
import com.rodemtree.yeyakitda.dto.EventDto;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.response.ResponseSuccessCode;
import com.rodemtree.yeyakitda.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @GetMapping("/banners")
    public ResponseEntity<ApiResponseDto<List<EventBannerDto>>> getInProgressBanners() {
        List<EventBannerDto> eventBannerDtos = eventService.findInProgressBanners();
        ApiResponseDto<List<EventBannerDto>> responseDto = ApiResponseDto.of(ResponseSuccessCode.EVENT_BANNERS, eventBannerDtos);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{eventId}")
    public ResponseEntity<ApiResponseDto<EventDto>> getEvent(@PathVariable Long eventId) {
        EventDto eventDto = eventService.findEvent(eventId);
        ApiResponseDto<EventDto> responseDto = ApiResponseDto.of(ResponseSuccessCode.EVENT_DETAIL, eventDto);
        return ResponseEntity.ok(responseDto);
    }

}
