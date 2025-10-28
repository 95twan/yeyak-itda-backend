package com.rodemtree.yeyakitda.repository;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rodemtree.yeyakitda.dto.EventBannerDto;
import com.rodemtree.yeyakitda.entity.QEventEntity;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class EventRepositoryCustomImpl implements EventRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<EventBannerDto> findInProgressBanners(LocalDateTime now) {
        QEventEntity event = QEventEntity.eventEntity;

        return jpaQueryFactory
                .select(Projections.constructor(EventBannerDto.class,
                        event.id,
                        event.title,
                        event.bannerImageUrl))
                .from(event)
                .where(event.startDate.loe(now)
                        .and(event.endDate.gt(now)))
                .orderBy(event.createdAt.desc())
                .limit(5)
                .fetch();
    }
}
