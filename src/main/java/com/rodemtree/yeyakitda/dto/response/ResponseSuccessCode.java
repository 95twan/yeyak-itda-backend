package com.rodemtree.yeyakitda.dto.response;

import lombok.Getter;
import org.springframework.http.HttpStatus;

public enum ResponseSuccessCode {
    SIGNUP(HttpStatus.CREATED.value(), "성공적으로 회원가입 되었습니다."),
    LOGIN(HttpStatus.OK.value(), "로그인 성공"),
    LOGOUT(HttpStatus.OK.value(), "성공적으로 로그아웃 되었습니다."),

    RESTAURANTS(HttpStatus.OK.value(), "성공적으로 식당 목록을 조회했습니다."),
    RESTAURANT(HttpStatus.OK.value(), "성공적으로 식당을 조회했습니다."),

    REISSUE_TOKEN(HttpStatus.OK.value(), "AccessToken이 성공적으로 발급되었습니다."),

    RESERVATION_CREATE(HttpStatus.CREATED.value(), "성공적으로 예약이 완료되었습니다."),
    USER_INFO(HttpStatus.OK.value(), "성공적으로 사용자 정보를 조회했습니다."),
    RESERVATION_SLOTS(HttpStatus.OK.value(), "성공적으로 예약 슬롯을 조회했습니다."),
    EVENT_BANNERS(HttpStatus.OK.value(), "성공적으로 이벤트 배너를 조회했습니다."),
    EVENT_DETAIL(HttpStatus.OK.value(), "성공적으로 이벤트 상세 정보를 조회했습니다."),
    RESERVATION_INFO(HttpStatus.OK.value(), "성공적으로 사용자 예약 정보를 조회했습니다."),
    RESERVATION_CANCEL(HttpStatus.OK.value(), "성공적으로 예약이 취소되었습니다."),
    REVIEW_CREATE(HttpStatus.CREATED.value(), "성공적으로 리뷰가 등록되었습니다.");


    @Getter
    private final int status;
    @Getter
    private final String message;

    ResponseSuccessCode(int status, String message) {
        this.status = status;
        this.message = message;
    }

}
