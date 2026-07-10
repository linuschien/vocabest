package com.vocabest.core.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public record WordMasteryRequest(
    @NotBlank String wordBankId,
    @NotNull Integer errorWeight,
    LocalDateTime nextReviewDate
) {}

