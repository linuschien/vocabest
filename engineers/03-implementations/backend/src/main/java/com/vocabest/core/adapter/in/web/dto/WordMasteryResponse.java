package com.vocabest.core.adapter.in.web.dto;

import java.time.LocalDateTime;
import java.util.UUID;

public record WordMasteryResponse(
    UUID id,
    String userId,
    String wordBankId,
    Integer errorWeight,
    LocalDateTime nextReviewDate
) {}
