package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("daily_progress")
public record DailyProgress(
    @Id UUID id,
    UUID userId,
    java.time.LocalDate date,
    Integer targetQuestions,
    Integer answeredQuestions,
    Integer correctQuestions,
    Integer wrongQuestions,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
) {}
