package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Table("word_mastery")
public record WordMastery(
    @Id @org.springframework.data.relational.core.mapping.Column("id") UUID id,
    UUID userId,
    UUID wordBankId,
    Integer errorWeight,
    LocalDateTime nextReviewDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
) {}
