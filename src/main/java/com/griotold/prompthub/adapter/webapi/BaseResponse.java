package com.griotold.prompthub.adapter.webapi;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

public record BaseResponse<T>(
        boolean success,
        T data
) {

    /**
     * 성공 응답을 위한 정적 팩토리 메서드 (200 OK)
     */
    public static <T> ResponseEntity<BaseResponse<T>> success(T data) {
        return ResponseEntity.ok(new BaseResponse<>(true, data));
    }

    /**
     * 생성 성공 응답을 위한 정적 팩토리 메서드 (201 Created)
     */
    public static <T> ResponseEntity<BaseResponse<T>> created(T data) {
        return ResponseEntity.status(HttpStatus.CREATED).body(new BaseResponse<>(true, data));
    }
}
