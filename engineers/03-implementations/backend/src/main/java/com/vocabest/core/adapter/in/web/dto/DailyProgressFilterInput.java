package com.vocabest.core.adapter.in.web.dto;

import java.util.UUID;

public record DailyProgressFilterInput(
    UUID id,
    UUID userId,
    String date,
    Integer targetQuestions,
    Integer answeredQuestions,
    Integer correctQuestions,
    Integer wrongQuestions
) {}
