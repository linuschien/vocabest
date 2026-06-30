package com.vocabest.core.adapter.in.web.dto;

import java.time.LocalDateTime;

public record DailyProgressRequest(
    LocalDateTime date,
    Integer completedQuestions
) {}
