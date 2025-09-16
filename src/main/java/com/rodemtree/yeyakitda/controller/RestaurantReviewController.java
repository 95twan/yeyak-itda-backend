package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.request.ReviewRequestDto;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.response.ResponseSuccessCode;
import com.rodemtree.yeyakitda.security.CustomUserDetails;
import com.rodemtree.yeyakitda.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/restaurants/{restaurantId}/reviews")
public class RestaurantReviewController {

    private final ReviewService reviewService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> createReview(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long restaurantId,
            @Valid @RequestPart("reviewData") ReviewRequestDto reviewRequestDto,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) {
        String userEmail = userDetails.getUsername();
        reviewService.createReview(restaurantId, userEmail, reviewRequestDto, images);

        ApiResponseDto<?> responseDto = ApiResponseDto.of(ResponseSuccessCode.REVIEW_CREATE);

        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
