package com.rodemtree.yeyakitda.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rodemtree.yeyakitda.entity.QRestaurantEntity;
import com.rodemtree.yeyakitda.entity.RestaurantEntity;
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
    public Page<RestaurantEntity> findByCategoriesAndKeyword(Set<String> categories, String keyword, Pageable pageable) {
        QRestaurantEntity restaurant = QRestaurantEntity.restaurantEntity;

        BooleanBuilder categoryBuilder = new BooleanBuilder();
        BooleanBuilder keywordBuilder = new BooleanBuilder();

        if (categories != null && !categories.isEmpty()) {
            for (String category : categories) {
                categoryBuilder.or(restaurant.category.containsIgnoreCase(category));
            }
        }

        if (keyword != null && !keyword.isEmpty()) {
            keywordBuilder.or(restaurant.name.containsIgnoreCase(keyword));
            keywordBuilder.or(restaurant.address.containsIgnoreCase(keyword));
            keywordBuilder.or(restaurant.description.containsIgnoreCase(keyword));
        }

        List<RestaurantEntity> content = jpaQueryFactory
                .selectFrom(restaurant)
                .where(categoryBuilder.and(keywordBuilder))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long count = jpaQueryFactory
                .select(restaurant.count())
                .from(restaurant)
                .where(categoryBuilder.and(keywordBuilder))
                .fetchOne();

        long total = count == null ? 0 : count;

        return new PageImpl<>(content, pageable, total);
    }
}
