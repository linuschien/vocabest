package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Table("quiz_question")
public record QuizQuestion(
    @Id @org.springframework.data.relational.core.mapping.Column("id") UUID id,
    UUID wordBankId,
    String contextualCloze,
    String chineseTranslation,
    String correctAnswer,
    String distractor1,
    String distractor2,
    String distractor3,
    String explanationRootAffix,
    String explanationMnemonic,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime deletedAt
) {}
