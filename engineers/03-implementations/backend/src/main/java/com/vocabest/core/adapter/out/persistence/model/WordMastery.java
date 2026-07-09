package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Column;

@Table("word_mastery")
public record WordMastery(
    @Id @Column("id") UUID id,
    @Column("user_id") UUID userId,
    @Column("word_bank_id") UUID wordBankId,
    @Column("error_weight") Integer errorWeight,
    @Column("next_review_date") LocalDateTime nextReviewDate,
    @Column("created_at") LocalDateTime createdAt,
    @Column("updated_at") LocalDateTime updatedAt,
    @Column("deleted_at") LocalDateTime deletedAt
) {}
