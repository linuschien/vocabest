package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Column;

@Table("error_event")
public record ErrorEvent(
    @Id @Column("id") UUID id,
    @Column("user_id") UUID userId,
    @Column("quiz_question_id") UUID quizQuestionId,
    @Column("timestamp") LocalDateTime timestamp,
    @Column("selected_distractor") String selectedDistractor,
    @Column("created_at") LocalDateTime createdAt,
    @Column("updated_at") LocalDateTime updatedAt,
    @Column("deleted_at") LocalDateTime deletedAt
) {}
