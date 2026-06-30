package com.vocabest.core.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ErrorLogResponse(
    UUID id,
    UUID wordId,
    Integer errorWeight,
    LocalDateTime nextReviewDate
) {}
