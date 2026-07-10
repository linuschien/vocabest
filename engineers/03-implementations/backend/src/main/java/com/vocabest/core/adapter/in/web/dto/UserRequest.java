package com.vocabest.core.adapter.in.web.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record UserRequest(
    @NotBlank @Email String email,
    @NotBlank String role,
    @NotBlank String targetLevel,
    @NotNull @Min(0) Integer learningStreak,
    @NotNull @Min(1) @Max(100) Integer dailyTargetQuestions
) {}

