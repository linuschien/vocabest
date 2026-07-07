package com.vocabest.core.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import com.vocabest.core.adapter.out.persistence.model.WordMastery;
import java.util.UUID;

import java.time.LocalDateTime;
import reactor.core.publisher.Mono;

public interface WordMasteryRepository extends R2dbcRepository<WordMastery, UUID> {

    Mono<WordMastery> findFirstByUserIdAndNextReviewDateLessThanEqualAndErrorWeightGreaterThanOrderByErrorWeightDescNextReviewDateAsc(UUID userId, LocalDateTime nextReviewDate, Integer errorWeight);
    
    Mono<Long> countByUserIdAndNextReviewDateLessThanEqualAndErrorWeightGreaterThan(UUID userId, LocalDateTime nextReviewDate, Integer errorWeight);
}
