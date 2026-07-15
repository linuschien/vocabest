package com.vocabest.core.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface WordBankRepository extends R2dbcRepository<WordBank, UUID>, WordBankRepositoryCustom {
    @Query("SELECT * FROM word_bank WHERE target_level = :targetLevel AND LENGTH(word) = 5 AND word NOT LIKE '% %' AND word NOT LIKE '%''%' AND word NOT LIKE '%-%' ORDER BY RANDOM() LIMIT 1")
    Mono<WordBank> findRandomWordleTarget(String targetLevel);
    
    @Query("SELECT * FROM word_bank WHERE target_level = :targetLevel AND LENGTH(word) BETWEEN 3 AND 10 AND word NOT LIKE '% %' AND word NOT LIKE '%''%' AND word NOT LIKE '%-%' ORDER BY RANDOM() LIMIT :limit")
    reactor.core.publisher.Flux<WordBank> findRandomCrosswordTargets(String targetLevel, int limit);
    
    Mono<Boolean> existsByWordAndTargetLevel(String word, TargetLevel targetLevel);
}
