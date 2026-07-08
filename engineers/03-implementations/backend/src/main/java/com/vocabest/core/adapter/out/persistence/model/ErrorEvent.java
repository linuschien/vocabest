package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("error_event")
public record ErrorEvent(
    @Id @org.springframework.data.relational.core.mapping.Column("id") UUID id,
    UUID userId,
    UUID quizQuestionId,
    LocalDateTime timestamp,
    String selectedDistractor,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
) {}
