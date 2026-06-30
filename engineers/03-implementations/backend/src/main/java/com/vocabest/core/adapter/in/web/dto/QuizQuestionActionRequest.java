package com.vocabest.core.adapter.in.web.dto;

public record QuizQuestionActionRequest(
    Integer maxWordsToProcess,
    String targetLevel
) {}
