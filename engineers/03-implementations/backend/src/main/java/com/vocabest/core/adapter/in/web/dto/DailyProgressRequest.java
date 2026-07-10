package com.vocabest.core.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record DailyProgressRequest(
    @NotNull LocalDate date,
    @NotNull @Min(1) Integer targetQuestions,
    @NotNull @Min(0) Integer answeredQuestions,
    @NotNull @Min(0) Integer correctQuestions,
    @NotNull @Min(0) Integer wrongQuestions
) {}

