package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.DailyProgressRequest;
import com.vocabest.core.adapter.in.web.dto.DailyProgressResponse;
import com.vocabest.core.adapter.out.persistence.model.DailyProgress;
import com.vocabest.core.adapter.out.persistence.repository.DailyProgressRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/daily-progresses")
@com.vocabest.core.adapter.in.web.security.RequireOwnership("#userId")
public class DailyProgressRestController {

    private final DailyProgressRepository repository;

    public DailyProgressRestController(DailyProgressRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Mono<ResponseEntity<DailyProgressResponse>> createDailyProgress(@PathVariable UUID userId, @Valid @RequestBody DailyProgressRequest req) {
        DailyProgress entity = new DailyProgress(null, userId, req.date(), req.targetQuestions(), req.answeredQuestions(), req.correctQuestions(), req.wrongQuestions(), null, null, null);
        return repository.save(entity)
                .map(this::mapToResponse)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<DailyProgressResponse>> getDailyProgressById(@PathVariable UUID userId, @PathVariable UUID id) {
        return repository.findById(id)
                .filter(dp -> dp.userId().equals(userId))
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<DailyProgressResponse>> updateDailyProgress(@PathVariable UUID userId, @PathVariable UUID id, @Valid @RequestBody DailyProgressRequest req) {
        return repository.findById(id)
                .filter(dp -> dp.userId().equals(userId))
                .map(existing -> new DailyProgress(existing.id(), userId, req.date(), req.targetQuestions(), req.answeredQuestions(), req.correctQuestions(), req.wrongQuestions(), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(repository::save)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteDailyProgress(@PathVariable UUID userId, @PathVariable UUID id) {
        return repository.findById(id)
                .filter(dp -> dp.userId().equals(userId))
                .flatMap(dp -> repository.deleteById(id))
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    private DailyProgressResponse mapToResponse(DailyProgress entity) {
        return new DailyProgressResponse(entity.id(), entity.date(), entity.targetQuestions(), entity.answeredQuestions(), entity.correctQuestions(), entity.wrongQuestions());
    }
}
