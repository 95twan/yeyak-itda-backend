package com.rodemtree.yeyakitda.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rodemtree.yeyakitda.entity.QThemeEntity;
import com.rodemtree.yeyakitda.entity.ThemeEntity;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class ThemeRepositoryCustomImpl implements ThemeRepositoryCustom {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public List<ThemeEntity> findInProgressThemes(LocalDateTime now) {
        QThemeEntity theme = QThemeEntity.themeEntity;

        return jpaQueryFactory
                .selectFrom(theme)
                .where(theme.startDate.loe(now)
                        .and(theme.endDate.gt(now)))
                .fetch();
    }
}
