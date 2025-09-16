package com.rodemtree.yeyakitda.dto.response;

import org.springframework.data.domain.Page;

public record PageInfoDto(
        Integer page,
        Integer size,
        Long totalElements,
        Integer totalPages
) {
    public static PageInfoDto of(Page<?> pageable) {
        return new PageInfoDto(pageable.getNumber(), pageable.getSize(), pageable.getTotalElements(), pageable.getTotalPages());
    }
}
