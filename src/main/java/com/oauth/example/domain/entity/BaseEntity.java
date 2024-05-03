package com.oauth.example.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.OffsetDateTime;
import java.util.UUID;

@MappedSuperclass
@Getter
@Setter
public class BaseEntity {
    @Id
    @Column(nullable = false, updatable = false)
    @GeneratedValue(generator = "uuid")
    private UUID id;

    @Column()
    @CreationTimestamp()
    private OffsetDateTime createdAt;

    @Column()
    @UpdateTimestamp
    private OffsetDateTime updatedAt;

    @Column()
    private OffsetDateTime deletedAt;
    @Column()
    private String createdBy;
    @Column()
    private String updatedBy;
}
