package com.griotold.prompthub.domain.category;

public class DuplicateCategoryNameException extends RuntimeException {

    public DuplicateCategoryNameException(String message) {
        super(message);
    }
}
