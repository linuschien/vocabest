package com.vocabest.core.application.service;

import com.vocabest.core.adapter.in.web.dto.*;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.repository.QuizQuestionRepository;
import com.vocabest.core.application.port.in.QuizQuestionCommandService;
import com.vocabest.core.application.port.in.QuizQuestionQueryService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class QuizQuestionServiceImpl implements QuizQuestionCommandService, QuizQuestionQueryService {

    private final QuizQuestionRepository repository;

    public QuizQuestionServiceImpl(QuizQuestionRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<QuizQuestionResponse> createQuizQuestion(QuizQuestionRequest req) {
        QuizQuestion entity = new QuizQuestion(null, UUID.fromString(req.wordId()), req.contextualCloze(),
                req.translation(), req.correctOption(), req.distractor1(), req.distractor2(), req.distractor3(),
                req.explanationRootAffix(), req.explanationMnemonic(), TargetLevel.valueOf(req.targetLevel()),
                null, null, null);
        return repository.save(entity).map(this::mapToResponse);
    }

    @Override
    public Mono<QuizQuestionResponse> updateQuizQuestion(UUID id, QuizQuestionRequest req) {
        return repository.findById(id)
                .map(existing -> new QuizQuestion(existing.id(), UUID.fromString(req.wordId()), req.contextualCloze(),
                        req.translation(), req.correctOption(), req.distractor1(), req.distractor2(), req.distractor3(),
                        req.explanationRootAffix(), req.explanationMnemonic(), TargetLevel.valueOf(req.targetLevel()),
                        existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(repository::save)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<Void> deleteQuizQuestion(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<OperationStatus> generateBatch(QuizQuestionActionRequest req) {
        return Mono.just(new OperationStatus(true, "Batch generated"));
    }

    @Override
    public Mono<QuizQuestionResponse> getQuizQuestionById(UUID id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public Flux<QuizQuestionResponse> listQuizQuestions(QuizQuestionFilterInput filter) {
        return repository.findAll().map(this::mapToResponse);
    }

    private QuizQuestionResponse mapToResponse(QuizQuestion entity) {
        return new QuizQuestionResponse(
                entity.id(),
                entity.vocabularyWordId().toString(),
                entity.contextualCloze(),
                entity.translation(),
                entity.correctOption(),
                entity.distractor1(),
                entity.distractor2(),
                entity.distractor3(),
                entity.explanationRootAffix(),
                entity.explanationMnemonic(),
                entity.targetLevel().name()
        );
    }
}
