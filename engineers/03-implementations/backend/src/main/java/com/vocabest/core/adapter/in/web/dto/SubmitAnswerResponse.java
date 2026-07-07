package com.vocabest.core.adapter.in.web.dto;

public record SubmitAnswerResponse(
    Boolean isCorrect,
    String correctAnswer,
    String explanationRootAffix,
    String explanationMnemonic
) {}
