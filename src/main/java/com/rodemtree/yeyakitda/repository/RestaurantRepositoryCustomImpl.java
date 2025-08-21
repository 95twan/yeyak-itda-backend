package com.rodemtree.yeyakitda.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
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
}
