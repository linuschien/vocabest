package com.vocabest.core.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record ErrorEventRequest(
    @NotNull UUID quizQuestionId,
    @NotNull LocalDateTime timestamp,
    @NotBlank String selectedDistractor
) {}

