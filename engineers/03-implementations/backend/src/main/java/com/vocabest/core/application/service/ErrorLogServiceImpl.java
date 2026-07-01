package com.vocabest.core.application.service;

import com.vocabest.core.adapter.in.web.dto.*;
import com.vocabest.core.adapter.out.persistence.model.ErrorLog;
import com.vocabest.core.adapter.out.persistence.repository.ErrorLogRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import com.vocabest.core.application.port.in.ErrorLogCommandService;
import com.vocabest.core.application.port.in.ErrorLogQueryService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class ErrorLogServiceImpl implements ErrorLogCommandService, ErrorLogQueryService {

    private final ErrorLogRepository repository;

    public ErrorLogServiceImpl(ErrorLogRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<ErrorLogResponse> createErrorLog(ErrorLogRequest req) {
        ErrorLog entity = new ErrorLog(null, null, req.wordId(), null,
                req.errorWeight(), req.nextReviewDate(), null, null, null);
        return repository.save(entity).map(this::mapToResponse);
    }

    @Override
    public Mono<ErrorLogResponse> updateErrorLog(UUID id, ErrorLogRequest req) {
        return repository.findById(id)
                .map(existing -> new ErrorLog(existing.id(), existing.userId(), req.wordId(), existing.quizQuestionId(),
                        req.errorWeight(), req.nextReviewDate(), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(repository::save)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<Void> deleteErrorLog(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<OperationStatus> recordFailure(UUID id, ErrorLogActionRequest req) {
        return repository.findById(id)
                .map(existing -> new ErrorLog(existing.id(), existing.userId(), existing.vocabularyWordId(), existing.quizQuestionId(),
                        existing.errorWeight() + 1, LocalDateTime.now().plusDays(existing.errorWeight() + 1), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(repository::save)
                .map(saved -> new OperationStatus(true, "Failure recorded. New weight: " + saved.errorWeight()));
    }

    @Override
    public Mono<ErrorLogResponse> getErrorLogById(UUID id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public Flux<ErrorLogResponse> listErrorLogs(ErrorLogFilterInput filter) {
        if (filter == null || filter.userId() == null) {
            return Flux.error(new IllegalArgumentException("Filter is required to prevent unauthorized data access"));
        }
        ErrorLog probe = new ErrorLog(null, filter.userId(), null, null, 0, null, null, null, null);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnorePaths("errorWeight");
        return repository.findAll(Example.of(probe, matcher)).map(this::mapToResponse);
    }

    private ErrorLogResponse mapToResponse(ErrorLog entity) {
        return new ErrorLogResponse(
                entity.id(),
                entity.vocabularyWordId(),
                entity.errorWeight(),
                entity.nextReviewDate()
        );
    }
}
