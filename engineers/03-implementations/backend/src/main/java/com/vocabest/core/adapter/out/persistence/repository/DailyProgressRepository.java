package com.vocabest.core.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import com.vocabest.core.adapter.out.persistence.model.DailyProgress;
import java.util.UUID;

import java.time.LocalDate;
import reactor.core.publisher.Mono;

public interface DailyProgressRepository extends R2dbcRepository<DailyProgress, UUID> {
    Mono<DailyProgress> findByUserIdAndDate(UUID userId, LocalDate date);
    reactor.core.publisher.Flux<DailyProgress> findByUserIdAndDateBetween(UUID userId, LocalDate startDate, LocalDate endDate);
    reactor.core.publisher.Flux<DailyProgress> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
