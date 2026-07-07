package com.vocabest.core.adapter.in.web.dto;

public record UserFilterInput(
    java.util.UUID id,
    String email,
    String role,
    String targetLevel,
    Integer learningStreak,
    Integer dailyTargetQuestions
) {}
