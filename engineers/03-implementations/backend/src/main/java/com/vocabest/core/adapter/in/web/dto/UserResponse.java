package com.vocabest.core.adapter.in.web.dto;

import java.util.UUID;

public record UserResponse(
    UUID id,
    String targetLevel,
    Integer learningStreak,
    Integer dailyTargetQuestions
) {}
