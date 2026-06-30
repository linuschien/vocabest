package com.vocabest.core.adapter.in.web.dto;

import java.util.UUID;

public record VocabularyWordResponse(
    UUID id,
    String word,
    String partOfSpeech,
    String translation,
    String level,
    Integer examFrequency
) {}
