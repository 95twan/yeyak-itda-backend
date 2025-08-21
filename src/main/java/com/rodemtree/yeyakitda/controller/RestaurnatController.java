package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.PageInfoDto;
import com.rodemtree.yeyakitda.dto.RestaurantDto;
import com.rodemtree.yeyakitda.dto.request.RestaurantSearchConditionDto;
import com.rodemtree.yeyakitda.dto.response.BaseResponseDto;
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
    public ResponseEntity<BaseResponseDto> getRestaurnatList(
            @ModelAttribute RestaurantSearchConditionDto condition,
            @PageableDefault(size = 12, page = 0, sort = {"rating"}, direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<RestaurantDto> restaurantList = restaurantService.getRestaurantList(condition, pageable);
        PageInfoDto pageInfoDto = PageInfoDto.of(restaurantList);
        BaseResponseDto responseDto = BaseResponseDto.of(HttpStatus.OK.value(), "성공적으로 식당 목록을 조회했습니다.", restaurantList.getContent(), pageInfoDto);

        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
