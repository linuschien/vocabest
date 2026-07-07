package com.vocabest.core.adapter.in.web.dto;

public record UserRequest(
    String email,
    String role,
    String targetLevel,
    Integer learningStreak,
    Integer dailyTargetQuestions
) {}
