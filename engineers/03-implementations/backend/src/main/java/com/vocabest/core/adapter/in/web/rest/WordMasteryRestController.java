package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.WordMasteryRequest;
import com.vocabest.core.adapter.in.web.dto.WordMasteryResponse;
import com.vocabest.core.adapter.out.persistence.model.WordMastery;
import com.vocabest.core.adapter.out.persistence.repository.WordMasteryRepository;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users/{userId}/wordMasteries")
@com.vocabest.core.adapter.in.web.security.RequireOwnership("#userId")
public class WordMasteryRestController {

    private final WordMasteryRepository repository;

    public WordMasteryRestController(WordMasteryRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Mono<ResponseEntity<WordMasteryResponse>> createWordMastery(@PathVariable UUID userId, @Valid @RequestBody WordMasteryRequest req) {
        WordMastery entity = new WordMastery(null, userId, UUID.fromString(req.wordBankId()), req.errorWeight(), req.nextReviewDate(), null, null, null);
        return repository.save(entity)
                .map(this::mapToResponse)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<WordMasteryResponse>> getWordMasteryById(@PathVariable UUID userId, @PathVariable UUID id) {
        return repository.findById(id)
                .filter(w -> w.userId().equals(userId))
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<WordMasteryResponse>> updateWordMastery(@PathVariable UUID userId, @PathVariable UUID id, @Valid @RequestBody WordMasteryRequest req) {
        return repository.findById(id)
                .filter(w -> w.userId().equals(userId))
                .map(existing -> new WordMastery(existing.id(), userId, UUID.fromString(req.wordBankId()), req.errorWeight(), req.nextReviewDate(), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(repository::save)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteWordMastery(@PathVariable UUID userId, @PathVariable UUID id) {
        return repository.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    private WordMasteryResponse mapToResponse(WordMastery entity) {
        return new WordMasteryResponse(entity.id(), entity.userId().toString(), entity.wordBankId().toString(), entity.errorWeight(), entity.nextReviewDate());
    }
}
