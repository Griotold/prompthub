package com.griotold.prompthub.adapter;

import com.fasterxml.jackson.annotation.JsonProperty;

public record ErrorResponse(
        @JsonProperty("success")
        boolean success,

        @JsonProperty("message")
        String message
) {
    // 에러는 항상 success: false
    public ErrorResponse(String message) {
        this(false, message);
    }
}
