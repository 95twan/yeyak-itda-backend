package com.rodemtree.yeyakitda.dto.request;

import jakarta.validation.constraints.*;

public record ReviewRequestDto(
        @NotNull(message = "별점은 필수 항목입니다.")
        @Min(value = 1, message = "별점은 1점 이상이어야 합니다.")
        @Max(value = 5, message = "별점은 5점 이하이어야 합니다.")
        Integer rating,

        @NotBlank(message = "리뷰 내용은 필수 항목입니다.")
        @Size(min = 10, max = 1000, message = "리뷰는 10자 이상 1000자 이하로 작성해야 합니다.")
        String comment
) {
    public static ReviewRequestDto of(Integer rating, String comment) {
        return new ReviewRequestDto(rating, comment);
    }
}
