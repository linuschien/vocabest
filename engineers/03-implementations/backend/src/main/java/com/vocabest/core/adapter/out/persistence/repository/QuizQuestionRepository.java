package com.vocabest.core.adapter.out.persistence.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.r2dbc.repository.Query;
import reactor.core.publisher.Flux;

public interface QuizQuestionRepository extends R2dbcRepository<QuizQuestion, UUID>, QuizQuestionRepositoryCustom {
    Flux<QuizQuestion> findAllBy(Pageable pageable);
    Flux<QuizQuestion> findByWordBankId(UUID wordBankId);

    @Query("SELECT DISTINCT q.* FROM quiz_question q JOIN error_event e ON q.id = e.quiz_question_id WHERE e.user_id = :userId AND q.word_bank_id = :wordBankId")
    Flux<QuizQuestion> findErrorQuestionsByUserAndWordBank(UUID userId, UUID wordBankId);
}
