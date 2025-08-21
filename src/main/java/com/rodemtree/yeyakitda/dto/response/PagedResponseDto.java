package com.rodemtree.yeyakitda.dto.response;

import org.springframework.data.domain.Page;

import java.util.List;

public record PagedResponseDto<T>(
        List<T> content,
        PageInfoDto pageInfo
) {
    public static <T> PagedResponseDto<T> of(Page<T> page) {
        PageInfoDto pageInfoDto = PageInfoDto.of(page);
        return new PagedResponseDto<>(page.getContent(), pageInfoDto);
    }
}
