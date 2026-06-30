package com.vocabest.core.adapter.in.web.dto;

public record VocabularyWordRequest(
    String word,
    String partOfSpeech,
    String translation,
    String level,
    Integer examFrequency
) {}
