package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("\"user\"")
public record User(
    @Id UUID id,
    TargetLevel targetLevel,
    Integer learningStreak,
    Integer dailyTargetQuestions,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
) {}
