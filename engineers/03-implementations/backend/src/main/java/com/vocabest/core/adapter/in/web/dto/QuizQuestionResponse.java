package com.vocabest.core.adapter.in.web.dto;

import java.util.UUID;

public record QuizQuestionResponse(
    UUID id,
    String wordId,
    String contextualCloze,
    String translation,
    String correctOption,
    String distractor1,
    String distractor2,
    String distractor3,
    String explanationRootAffix,
    String explanationMnemonic,
    String targetLevel
) {}
