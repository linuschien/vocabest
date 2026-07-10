package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.ErrorEventRequest;
import com.vocabest.core.adapter.in.web.dto.ErrorEventResponse;
import com.vocabest.core.adapter.out.persistence.model.ErrorEvent;
import com.vocabest.core.adapter.out.persistence.repository.ErrorEventRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/errorEvents")
@com.vocabest.core.adapter.in.web.security.RequireOwnership("#userId")
public class ErrorEventRestController {

    private final ErrorEventRepository repository;

    public ErrorEventRestController(ErrorEventRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Mono<ResponseEntity<ErrorEventResponse>> createErrorEvent(@PathVariable UUID userId, @Valid @RequestBody ErrorEventRequest req) {
        ErrorEvent entity = new ErrorEvent(null, userId, req.quizQuestionId(), req.timestamp(), req.selectedDistractor(), null, null, null);
        return repository.save(entity)
                .map(this::mapToResponse)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<ErrorEventResponse>> getErrorEventById(@PathVariable UUID userId, @PathVariable UUID id) {
        return repository.findById(id)
                .filter(e -> e.userId().equals(userId))
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<ErrorEventResponse>> updateErrorEvent(@PathVariable UUID userId, @PathVariable UUID id, @Valid @RequestBody ErrorEventRequest req) {
        return repository.findById(id)
                .filter(e -> e.userId().equals(userId))
                .map(existing -> new ErrorEvent(existing.id(), userId, req.quizQuestionId(), req.timestamp(), req.selectedDistractor(), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(repository::save)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteErrorEvent(@PathVariable UUID userId, @PathVariable UUID id) {
        return repository.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    private ErrorEventResponse mapToResponse(ErrorEvent entity) {
        return new ErrorEventResponse(entity.id(), entity.userId(), entity.quizQuestionId(), entity.timestamp(), entity.selectedDistractor());
    }
}
