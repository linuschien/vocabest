package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("quiz_question")
public record QuizQuestion(
    @Id UUID id,
    UUID vocabularyWordId,
    String contextualCloze,
    String translation,
    String correctOption,
    String distractor1,
    String distractor2,
    String distractor3,
    String explanationRootAffix,
    String explanationMnemonic,
    TargetLevel targetLevel,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
) {}
