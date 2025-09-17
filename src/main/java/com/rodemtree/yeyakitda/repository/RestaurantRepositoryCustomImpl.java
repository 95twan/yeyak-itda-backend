package com.rodemtree.yeyakitda.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.PathBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.entity.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Set;

@RequiredArgsConstructor
public class RestaurantRepositoryCustomImpl implements RestaurantRepositoryCustom{

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<RestaurantEntity> search(RestaurantSearchConditionDto condition, Pageable pageable) {
        QRestaurantEntity restaurant = QRestaurantEntity.restaurantEntity;

        BooleanBuilder builder = new BooleanBuilder();

        Set<String> categories = condition.categories();

        if (categories != null && !categories.isEmpty()) {
            BooleanBuilder categoryBuilder = new BooleanBuilder();
            for (String category : categories) {
                categoryBuilder.or(restaurant.category.containsIgnoreCase(category));
            }
            builder.and(categoryBuilder);
        }

        String keyword = condition.keyword();
        if (keyword != null && !keyword.isBlank()) {
            BooleanBuilder keywordBuilder = new BooleanBuilder();
            keywordBuilder.or(restaurant.name.containsIgnoreCase(keyword));
            keywordBuilder.or(restaurant.address.containsIgnoreCase(keyword));
            keywordBuilder.or(restaurant.description.containsIgnoreCase(keyword));
            builder.and(keywordBuilder);
        }

        List<RestaurantEntity> content = jpaQueryFactory
                .selectFrom(restaurant)
                .leftJoin(restaurant.thumbnailImage).fetchJoin()
                .where(builder)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = jpaQueryFactory
                .select(restaurant.count())
                .from(restaurant)
                .where(builder)
                .fetchOne();

        long total = count == null ? 0 : count;

        return new PageImpl<>(content, pageable, total);
    }

    // Todo - test 작성
    @Override
    public List<RestaurantEntity> findTop10ByTheme(ThemeEntity theme) {
        QRestaurantThemeMappingEntity mapping = QRestaurantThemeMappingEntity.restaurantThemeMappingEntity;
        QRestaurantEntity restaurant = QRestaurantEntity.restaurantEntity;

        return jpaQueryFactory
                .select(mapping.restaurant)
                .from(mapping)
                .join(mapping.restaurant, restaurant)
                .leftJoin(restaurant.thumbnailImage).fetchJoin()
                .where(mapping.theme.eq(theme))
                .orderBy(mapping.id.desc())
                .limit(10)
                .fetch();
    }

    @Override
    public Page<RestaurantEntity> findByTheme(String themeTitle, Pageable pageable) {
        QRestaurantThemeMappingEntity mapping = QRestaurantThemeMappingEntity.restaurantThemeMappingEntity;
        QThemeEntity theme = QThemeEntity.themeEntity;

        List<RestaurantEntity> content = jpaQueryFactory
                .select(mapping.restaurant)
                .from(mapping)
                .join(mapping.theme, theme)
                .leftJoin(mapping.restaurant.thumbnailImage).fetchJoin()
                .where(mapping.theme.title.eq(themeTitle))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .orderBy(mapping.restaurant.rating.desc())
                .fetch();

        Long count = jpaQueryFactory
                .select(mapping.restaurant.count())
                .from(mapping)
                .join(mapping.theme, theme)
                .where(mapping.theme.title.eq(themeTitle))
                .fetchOne();

        long total = count == null ? 0 : count;

        return new PageImpl<>(content, pageable, total);
    }
}
