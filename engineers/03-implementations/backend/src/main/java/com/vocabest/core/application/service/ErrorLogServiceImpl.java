package com.vocabest.core.application.service;

import com.vocabest.core.adapter.in.web.dto.*;
import com.vocabest.core.adapter.out.persistence.model.ErrorLog;
import com.vocabest.core.adapter.out.persistence.repository.ErrorLogRepository;
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
        // Implementation logic for recording failure
        return Mono.just(new OperationStatus(true, "Failure recorded"));
    }

    @Override
    public Mono<ErrorLogResponse> getErrorLogById(UUID id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public Flux<ErrorLogResponse> listErrorLogs(ErrorLogFilterInput filter) {
        // In real implementation, use Example matcher
        return repository.findAll().map(this::mapToResponse);
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
