package com.vocabest.core.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record DailyProgressResponse(
    UUID id,
    LocalDateTime date,
    Integer completedQuestions
) {}
