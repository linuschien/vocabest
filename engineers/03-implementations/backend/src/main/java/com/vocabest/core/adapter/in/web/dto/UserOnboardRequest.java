package com.vocabest.core.adapter.in.web.dto;

public record UserOnboardRequest(
    String email,
    String targetLevel,
    Integer dailyTargetQuestions
) {}
