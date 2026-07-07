package com.vocabest.core.adapter.in.web.dto;

import java.util.UUID;

public record ErrorEventFilterInput(
    UUID id,
    UUID userId,
    UUID quizQuestionId,
    java.time.LocalDateTime timestamp,
    String selectedDistractor
) {}
