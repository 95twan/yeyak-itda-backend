package com.rodemtree.yeyakitda.controller;

import com.rodemtree.yeyakitda.dto.request.ReservationRequestDto;
import com.rodemtree.yeyakitda.dto.response.ApiResponseDto;
import com.rodemtree.yeyakitda.dto.response.ResponseSuccessCode;
import com.rodemtree.yeyakitda.security.CustomUserDetails;
import com.rodemtree.yeyakitda.service.ReservationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/reservations")
public class ReservationController {

    private final ReservationService reservationService;

    @PostMapping
    public ResponseEntity<ApiResponseDto<?>> createReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody ReservationRequestDto reservationRequestDto
    ) {
        String userEmail = userDetails.getUsername();
        reservationService.createReservation(userEmail, reservationRequestDto);
        ApiResponseDto<?> responseDto = ApiResponseDto.of(ResponseSuccessCode.RESERVATION_CREATE);
        return ResponseEntity.status(201).body(responseDto);
    }

    @DeleteMapping("/{reservationId}")
    public ResponseEntity<ApiResponseDto<?>> cancelReservation(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long reservationId
    ) {
        String userEmail = userDetails.getUsername();
        reservationService.cancelReservation(userEmail, reservationId);
        ApiResponseDto<?> responseDto = ApiResponseDto.of(ResponseSuccessCode.RESERVATION_CANCEL);
        return ResponseEntity.ok(responseDto);
    }
}
