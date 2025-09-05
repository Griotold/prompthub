package com.griotold.prompthub.adapter.webapi.dto;

public record PaginationInfo(
        int currentPage,
        int totalPages,
        long totalCount,
        boolean hasNext,
        boolean hasPrevious
) {}
