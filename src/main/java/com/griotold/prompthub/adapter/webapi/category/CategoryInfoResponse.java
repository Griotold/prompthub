package com.griotold.prompthub.adapter.webapi.category;

import com.griotold.prompthub.domain.category.Category;

public record CategoryInfoResponse(
        Long id,
        String name,
        String description,
        boolean active
) {
    public static CategoryInfoResponse of(Category category) {
        return new CategoryInfoResponse(
                category.getId(),
                category.getName(),
                category.getDescription(),
                category.getIsActive()
        );
    }
}
