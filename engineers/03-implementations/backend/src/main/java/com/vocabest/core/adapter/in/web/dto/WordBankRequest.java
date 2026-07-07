package com.vocabest.core.adapter.in.web.dto;

public record WordBankRequest(
    String word,
    String partsOfSpeech,
    String chineseTranslation,
    String targetLevel,
    Integer difficultyLevel,
    Integer examFrequency
) {}
