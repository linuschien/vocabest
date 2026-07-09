package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Column;

@Table("user")
public record User(
    @Id @Column("id") UUID id,
    @Column("email") String email,
    @Column("role") Role role,
    @Column("target_level") TargetLevel targetLevel,
    @Column("learning_streak") Integer learningStreak,
    @Column("daily_target_questions") Integer dailyTargetQuestions,
    @Column("created_at") LocalDateTime createdAt,
    @Column("updated_at") LocalDateTime updatedAt,
    @Column("deleted_at") LocalDateTime deletedAt
) {}
