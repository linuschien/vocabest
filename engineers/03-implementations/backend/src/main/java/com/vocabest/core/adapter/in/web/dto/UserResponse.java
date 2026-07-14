package com.vocabest.core.adapter.in.web.dto;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String email,
    String role,
    String targetLevel,
    Integer learningStreak,
    Integer maxLearningStreak,
    Integer maxDailyQuestions,
    Integer dailyTargetQuestions
) {}
