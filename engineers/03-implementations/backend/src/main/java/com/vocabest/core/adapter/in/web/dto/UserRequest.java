package com.vocabest.core.adapter.in.web.dto;

public record UserRequest(
    String targetLevel,
    Integer learningStreak,
    Integer dailyTargetQuestions
) {}
