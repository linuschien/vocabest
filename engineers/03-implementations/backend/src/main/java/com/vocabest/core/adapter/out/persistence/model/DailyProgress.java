package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Column;

@Table("daily_progress")
public record DailyProgress(
    @Id @Column("id") UUID id,
    @Column("user_id") UUID userId,
    @Column("date") java.time.LocalDate date,
    @Column("target_questions") Integer targetQuestions,
    @Column("answered_questions") Integer answeredQuestions,
    @Column("correct_questions") Integer correctQuestions,
    @Column("wrong_questions") Integer wrongQuestions,
    @Column("created_at") LocalDateTime createdAt,
    @Column("updated_at") LocalDateTime updatedAt,
    @Column("deleted_at") LocalDateTime deletedAt
) {}
