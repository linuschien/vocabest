package com.vocabest.core.adapter.in.web.dto;

public record QuizQuestionRequest(
    String contextualCloze,
    String chineseTranslation,
    String correctAnswer,
    String distractor1,
    String distractor2,
    String distractor3,
    String explanationRootAffix,
    String explanationMnemonic
) {}
