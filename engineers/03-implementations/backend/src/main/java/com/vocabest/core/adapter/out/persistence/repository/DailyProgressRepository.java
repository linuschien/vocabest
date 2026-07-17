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

    @org.springframework.data.r2dbc.repository.Query("SELECT user_id, COALESCE(SUM(answered_questions), 0) AS total_answered, COALESCE(SUM(correct_questions), 0) AS total_correct FROM daily_progress WHERE user_id IN (:userIds) GROUP BY user_id")
    reactor.core.publisher.Flux<UserAggregatedStats> findAggregatedStatsByUserIds(java.util.List<UUID> userIds);
}
