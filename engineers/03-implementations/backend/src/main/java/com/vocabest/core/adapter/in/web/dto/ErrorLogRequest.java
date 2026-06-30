package com.vocabest.core.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record ErrorLogRequest(
    UUID wordId,
    Integer errorWeight,
    LocalDateTime nextReviewDate
) {}
