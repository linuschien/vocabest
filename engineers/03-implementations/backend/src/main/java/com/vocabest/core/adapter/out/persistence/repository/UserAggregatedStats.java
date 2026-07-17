package com.vocabest.core.adapter.out.persistence.repository;

import java.util.UUID;

public record UserAggregatedStats(
    UUID userId,
    Integer totalAnswered,
    Integer totalCorrect
) {}
