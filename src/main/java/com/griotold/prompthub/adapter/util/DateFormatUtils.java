package com.griotold.prompthub.adapter.util;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class DateFormatUtils {

    private static final DateTimeFormatter REVIEW_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyy.MM.dd.");

    private DateFormatUtils() {}

    public static String formatReviewDate(LocalDateTime dateTime) {
        if (dateTime == null) {
            return "";
        }
        return dateTime.format(REVIEW_DATE_FORMAT);
    }
}
