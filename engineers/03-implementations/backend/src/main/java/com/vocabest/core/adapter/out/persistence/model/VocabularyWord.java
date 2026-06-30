package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("vocabulary_word")
public record VocabularyWord(
    @Id UUID id,
    String word,
    String partOfSpeech,
    String translation,
    VocabularyLevel level,
    Integer examFrequency,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
) {}
