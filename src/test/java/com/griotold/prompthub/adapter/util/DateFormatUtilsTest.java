package com.griotold.prompthub.adapter.util;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
class DateFormatUtilsTest {

    @Test
    void formatReviewDate_정상_날짜_포맷팅() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2025, 9, 19, 14, 30, 45);
        log.info("dateTime={}", dateTime);

        // When
        String result = DateFormatUtils.formatReviewDate(dateTime);

        // Then
        assertThat(result).isEqualTo("2025.09.19.");
    }

    @Test
    void formatReviewDate_한자리_월일_포맷팅() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2025, 3, 5, 9, 15, 20);
        log.info("dateTime={}", dateTime);

        // When
        String result = DateFormatUtils.formatReviewDate(dateTime);

        // Then
        assertThat(result).isEqualTo("2025.03.05.");
    }

    @Test
    void formatReviewDate_null_입력시_빈문자열_반환() {
        // Given
        LocalDateTime dateTime = null;

        // When
        String result = DateFormatUtils.formatReviewDate(dateTime);

        // Then
        assertThat(result).isEqualTo("");
    }

    @Test
    void formatReviewDate_연말_날짜_포맷팅() {
        // Given
        LocalDateTime dateTime = LocalDateTime.of(2024, 12, 31, 23, 59, 59);
        log.info("dateTime={}", dateTime);

        // When
        String result = DateFormatUtils.formatReviewDate(dateTime);

        // Then
        assertThat(result).isEqualTo("2024.12.31.");
    }
}