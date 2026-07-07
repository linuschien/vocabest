package com.vocabest.core.adapter.in.web.dto;

import java.time.LocalDateTime;

public record WordMasteryRequest(
    String wordBankId,
    Integer errorWeight,
    LocalDateTime nextReviewDate
) {}
