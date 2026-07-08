package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("word_bank")
public record WordBank(
    @Id @org.springframework.data.relational.core.mapping.Column("id") UUID id,
    String word,
    String partsOfSpeech,
    String chineseTranslation,
    TargetLevel targetLevel,
    Integer difficultyLevel,
    Integer examFrequency,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
) {}
