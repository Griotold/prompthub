package com.griotold.prompthub.adapter.webapi.dto;

import org.springframework.data.domain.Page;

import java.util.List;

public record PageResponse<T>(
        List<T> content,
        PaginationInfo pagination
) {
    public static <T> PageResponse<T> of(Page<T> page) {
        PaginationInfo pagination = new PaginationInfo(
                page.getNumber() + 1,    // 0-based → 1-based
                page.getTotalPages(),
                page.getTotalElements(),
                page.hasNext(),
                page.hasPrevious()
        );

        return new PageResponse<>(page.getContent(), pagination);
    }
}
