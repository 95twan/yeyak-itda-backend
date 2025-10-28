package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.dto.EventBannerDto;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<EventBannerDto> findInProgressBanners(LocalDateTime now);
}
