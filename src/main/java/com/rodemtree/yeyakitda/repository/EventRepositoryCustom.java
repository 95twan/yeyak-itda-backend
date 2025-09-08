package com.rodemtree.yeyakitda.repository;

import com.rodemtree.yeyakitda.dto.EventBannerDto;
import com.rodemtree.yeyakitda.entity.EventEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface EventRepositoryCustom {
    List<EventBannerDto> findInProgressBanners(LocalDateTime now);
}
