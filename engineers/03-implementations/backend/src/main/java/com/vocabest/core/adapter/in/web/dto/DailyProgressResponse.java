package com.vocabest.core.adapter.in.web.dto;

import java.time.LocalDate;
import java.util.UUID;
import java.time.LocalDateTime;

public record DailyProgressResponse(
    UUID id,
    LocalDate date,
    Integer targetQuestions,
    Integer answeredQuestions,
    Integer correctQuestions,
    Integer wrongQuestions
) {}
