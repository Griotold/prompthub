package com.griotold.prompthub.domain.tag;

import com.griotold.prompthub.domain.AbstractEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "p_tag")
@ToString(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Tag extends AbstractEntity {

    @Column(length = 50, nullable = false, unique = true)
    private String name;

    @CreatedDate
    private LocalDateTime createdAt;

    public static Tag create(String name) {
        Tag tag = new Tag();
        tag.name = name;
        return tag;
    }
}
