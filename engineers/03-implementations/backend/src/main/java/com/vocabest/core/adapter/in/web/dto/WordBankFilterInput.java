package com.vocabest.core.adapter.in.web.dto;

public record WordBankFilterInput(
    String word,
    String startingLetter,
    Integer difficultyLevel,
    String targetLevel,
    Integer page,
    Integer size
) {}
