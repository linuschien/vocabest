package com.vocabest.core.adapter.in.web.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserOnboardRequest(
    String email,
    @NotBlank String targetLevel,
    @NotNull @Min(1) @Max(100) Integer dailyTargetQuestions
) {}

