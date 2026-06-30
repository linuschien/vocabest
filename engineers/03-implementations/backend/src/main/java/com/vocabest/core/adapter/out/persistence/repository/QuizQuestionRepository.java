package com.vocabest.core.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.ReactiveQueryByExampleExecutor;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import java.util.UUID;

public interface QuizQuestionRepository extends R2dbcRepository<QuizQuestion, UUID>, ReactiveQueryByExampleExecutor<QuizQuestion> {
}
