package com.vocabest.core.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ErrorEventRequest(
    UUID quizQuestionId,
    LocalDateTime timestamp,
    String selectedDistractor
) {}
