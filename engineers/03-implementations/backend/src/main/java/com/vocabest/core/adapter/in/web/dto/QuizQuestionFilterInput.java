package com.vocabest.core.adapter.in.web.dto;

public record QuizQuestionFilterInput(
    java.util.UUID wordBankId,
    String word,
    String startingLetter,
    Integer difficultyLevel,
    String targetLevel,
    Integer page,
    Integer size
) {}
