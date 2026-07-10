package com.vocabest.core.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record QuizQuestionRequest(
    @NotBlank String contextualCloze,
    @NotBlank String chineseTranslation,
    @NotBlank String correctAnswer,
    @NotBlank String distractor1,
    @NotBlank String distractor2,
    @NotBlank String distractor3,
    @NotBlank String explanationRootAffix,
    @NotBlank String explanationMnemonic
) {}

