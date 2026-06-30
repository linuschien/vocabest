package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("error_log")
public record ErrorLog(
    @Id UUID id,
    UUID userId,
    UUID vocabularyWordId,
    UUID quizQuestionId,
    Integer errorWeight,
    LocalDateTime nextReviewDate,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
) {}
