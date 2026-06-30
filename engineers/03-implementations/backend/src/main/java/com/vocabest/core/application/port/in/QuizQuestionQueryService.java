package com.vocabest.core.application.port.in;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionResponse;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionFilterInput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface QuizQuestionQueryService {
    Mono<QuizQuestionResponse> getQuizQuestionById(UUID id);
    Flux<QuizQuestionResponse> listQuizQuestions(QuizQuestionFilterInput filter);
}
