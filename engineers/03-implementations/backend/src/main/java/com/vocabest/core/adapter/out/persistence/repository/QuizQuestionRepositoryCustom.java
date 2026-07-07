package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionFilterInput;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface QuizQuestionRepositoryCustom {
    Flux<QuizQuestion> search(QuizQuestionFilterInput filter);
    Mono<Long> count(QuizQuestionFilterInput filter);
}
