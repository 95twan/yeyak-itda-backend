package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.response.PagedResponseDto;
import com.rodemtree.yeyakitda.service.RestaurantService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/restaurants")
@RequiredArgsConstructor
public class RestaurnatController {

    private final RestaurantService restaurantService;

    @GetMapping
    public ResponseEntity<ApiResponseDto<PagedResponseDto<RestaurantDto>>> getRestaurnatList(
            @ModelAttribute RestaurantSearchConditionDto condition,
            @PageableDefault(size = 12, page = 0, sort = {"rating"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RestaurantDto> restaurantList = restaurantService.getRestaurants(condition, pageable);
        PagedResponseDto<RestaurantDto> pagedResponseDto = PagedResponseDto.of(restaurantList);
        ApiResponseDto<PagedResponseDto<RestaurantDto>> responseDto = ApiResponseDto.of(HttpStatus.OK.value(), "성공적으로 식당 목록을 조회했습니다.", pagedResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
