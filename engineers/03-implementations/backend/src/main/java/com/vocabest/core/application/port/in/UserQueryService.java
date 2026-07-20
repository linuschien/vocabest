package com.vocabest.core.application.port.in;

import com.vocabest.core.adapter.in.web.dto.ErrorReviewCountResponse;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionResponse;
import com.vocabest.core.adapter.in.web.dto.UserFilterInput;
import com.vocabest.core.adapter.out.persistence.model.User;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserQueryService {
    Mono<User> getUserById(UUID id);
    Flux<User> listUsers(UserFilterInput filter);
    Mono<QuizQuestionResponse> getNextQuestion(UUID userId);
    
    Mono<QuizQuestionResponse> getNextErrorQuestion(UUID userId);
    
    Mono<User> whoami();
    
    Mono<com.vocabest.core.adapter.in.web.dto.WordBankResponse> getWordleTarget(UUID userId);
    
    Mono<com.vocabest.core.adapter.in.web.dto.WordleValidationResponse> validateWordleGuess(UUID userId, String guess);
    
    Flux<com.vocabest.core.adapter.in.web.dto.WordBankResponse> getCrosswordTargets(UUID userId, int count);
    
    Flux<com.vocabest.core.adapter.in.web.dto.WordBankResponse> getMemoryGameTargets(UUID userId, int count);
    
    Mono<ErrorReviewCountResponse> getErrorReviewCount(UUID userId);
}
