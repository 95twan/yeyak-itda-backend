package com.rodemtree.yeyakitda.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

public class DuplicateException extends RuntimeException{

    @Getter
    @RequiredArgsConstructor
    public enum Field {
        EMAIL("이메일"),
        NICKNAME("닉네임"),
        PHONE_NUMBER("전화번호");

        private final String description; // 한글 설명 등 추가 정보 포함 가능
    }

    public DuplicateException(List<Field> duplicateFields) {
        super(createMessage(duplicateFields));
    }

    private static String createMessage(List<Field> duplicateFields) {
        String fieldNames = duplicateFields.stream()
                .map(Field::getDescription)
                .collect(Collectors.joining(", "));
        return "중복된 항목: [" + fieldNames + "]";
    }
}
