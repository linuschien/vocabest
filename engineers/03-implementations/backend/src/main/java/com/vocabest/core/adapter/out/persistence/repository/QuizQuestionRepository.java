package com.vocabest.core.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;

public interface QuizQuestionRepository extends R2dbcRepository<QuizQuestion, UUID>, QuizQuestionRepositoryCustom {
    Flux<QuizQuestion> findAllBy(Pageable pageable);
}
