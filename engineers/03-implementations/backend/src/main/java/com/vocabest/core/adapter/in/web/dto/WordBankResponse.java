package com.vocabest.core.adapter.in.web.dto;

import java.util.UUID;

public record WordBankResponse(
    UUID id,
    String word,
    String partsOfSpeech,
    String chineseTranslation,
    String targetLevel,
    Integer difficultyLevel,
    Integer examFrequency
) {}
