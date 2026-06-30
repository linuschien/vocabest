package com.vocabest.core.application.port.in;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionRequest;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionResponse;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionActionRequest;
import com.vocabest.core.adapter.in.web.dto.OperationStatus;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface QuizQuestionCommandService {
    Mono<QuizQuestionResponse> createQuizQuestion(QuizQuestionRequest req);
    Mono<QuizQuestionResponse> updateQuizQuestion(UUID id, QuizQuestionRequest req);
    Mono<Void> deleteQuizQuestion(UUID id);
    Mono<OperationStatus> generateBatch(QuizQuestionActionRequest req);
}
