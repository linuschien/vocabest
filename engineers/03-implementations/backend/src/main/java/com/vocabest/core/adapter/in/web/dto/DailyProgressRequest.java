package com.vocabest.core.adapter.in.web.dto;

import java.time.LocalDate;

public record DailyProgressRequest(
    LocalDate date,
    Integer targetQuestions,
    Integer answeredQuestions,
    Integer correctQuestions,
    Integer wrongQuestions
) {}
