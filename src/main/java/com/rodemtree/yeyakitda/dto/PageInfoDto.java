package com.rodemtree.yeyakitda.dto;

import org.springframework.data.domain.Page;

public record PageInfoDto(
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages
) {
    public static PageInfoDto of(Page<RestaurantDto> pageable) {
        return new PageInfoDto(pageable.getNumber(), pageable.getSize(), pageable.getTotalElements(), pageable.getTotalPages());
    }
}
