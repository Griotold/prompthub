package com.griotold.prompthub.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

import static java.util.Objects.requireNonNull;

@Entity
@Table(name = "p_category")
@Getter
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Category extends AbstractEntity {

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    private Boolean isActive = true;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    public static Category create(CategoryRegisterRequest registerRequest) {
        Category category = new Category();

        category.name = requireNonNull(registerRequest.name());
        category.description = requireNonNull(registerRequest.description());
        category.isActive = true;

        return category;
    }

    public void deactivate() {
        this.isActive = false;
    }

    public void activate() {
        this.isActive = true;
    }

    public void updateInfo(CategoryUpdateRequest updateRequest) {
        name = requireNonNull(updateRequest.name());
        description = requireNonNull(updateRequest.description());
    }
}