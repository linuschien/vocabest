package com.vocabest.core.adapter.out.persistence.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.data.relational.core.mapping.Column;

@Table("quiz_question")
public record QuizQuestion(
    @Id @Column("id") UUID id,
    @Column("word_bank_id") UUID wordBankId,
    @Column("contextual_cloze") String contextualCloze,
    @Column("chinese_translation") String chineseTranslation,
    @Column("correct_answer") String correctAnswer,
    @Column("distractor1") String distractor1,
    @Column("distractor2") String distractor2,
    @Column("distractor3") String distractor3,
    @Column("explanation_root_affix") String explanationRootAffix,
    @Column("explanation_mnemonic") String explanationMnemonic,
    @Column("created_at") LocalDateTime createdAt,
    @Column("updated_at") LocalDateTime updatedAt,
    @Column("deleted_at") LocalDateTime deletedAt
) {}
