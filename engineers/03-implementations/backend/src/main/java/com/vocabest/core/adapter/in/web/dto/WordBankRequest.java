package com.vocabest.core.adapter.in.web.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record WordBankRequest(
    @NotBlank String word,
    @NotBlank String partsOfSpeech,
    @NotBlank String chineseTranslation,
    @NotBlank String targetLevel,
    @NotNull @Min(1) Integer difficultyLevel,
    @NotNull @Min(0) Integer examFrequency
) {}

