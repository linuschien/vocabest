package com.vocabest.core.adapter.in.web.dto;

import java.util.UUID;

public record WordMasteryFilterInput(
    UUID id,
    UUID userId,
    UUID wordBankId,
    Integer errorWeight
) {}
