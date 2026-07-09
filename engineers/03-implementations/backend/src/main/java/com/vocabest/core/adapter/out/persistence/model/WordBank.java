package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Column;

@Table("word_bank")
public record WordBank(
    @Id @Column("id") UUID id,
    @Column("word") String word,
    @Column("parts_of_speech") String partsOfSpeech,
    @Column("chinese_translation") String chineseTranslation,
    @Column("target_level") TargetLevel targetLevel,
    @Column("difficulty_level") Integer difficultyLevel,
    @Column("exam_frequency") Integer examFrequency,
    @Column("created_at") LocalDateTime createdAt,
    @Column("updated_at") LocalDateTime updatedAt,
    @Column("deleted_at") LocalDateTime deletedAt
) {}
