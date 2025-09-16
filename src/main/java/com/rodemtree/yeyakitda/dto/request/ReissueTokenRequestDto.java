package com.rodemtree.yeyakitda.dto.request;

import jakarta.validation.constraints.NotBlank;

public record ReissueTokenRequestDto(
        @NotBlank String refreshToken
) {
}
