package com.vocabest.core.adapter.in.web.dto;

import java.util.UUID;
import java.time.LocalDateTime;

public record ErrorEventFilterInput(
    UUID id,
    UUID userId,
    UUID quizQuestionId,
    LocalDateTime timestamp,
    String selectedDistractor,
    String startDate,
    String endDate,
    Integer page,
    Integer size
) {}
