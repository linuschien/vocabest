package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.WordBankRequest;
import com.vocabest.core.adapter.in.web.dto.WordBankResponse;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import com.vocabest.core.adapter.out.persistence.repository.WordBankRepository;
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
@RequestMapping("/api/v1/word-banks")
public class WordBankRestController {

    private final WordBankRepository repository;

    public WordBankRestController(WordBankRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    @com.vocabest.core.adapter.in.web.security.AdminOnly
    public Mono<ResponseEntity<WordBankResponse>> createWordBank(@Valid @RequestBody WordBankRequest req) {
        WordBank entity = new WordBank(null, req.word(), req.partsOfSpeech(), req.chineseTranslation(), TargetLevel.valueOf(req.targetLevel()), req.difficultyLevel(), req.examFrequency(), null, null, null);
        return repository.save(entity)
                .map(this::mapToResponse)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<WordBankResponse>> getWordBankById(@PathVariable UUID id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    @com.vocabest.core.adapter.in.web.security.AdminOnly
    public Mono<ResponseEntity<WordBankResponse>> updateWordBank(@PathVariable UUID id, @Valid @RequestBody WordBankRequest req) {
        return repository.findById(id)
                .map(existing -> new WordBank(existing.id(), req.word(), req.partsOfSpeech(), req.chineseTranslation(), TargetLevel.valueOf(req.targetLevel()), req.difficultyLevel(), req.examFrequency(), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(repository::save)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @com.vocabest.core.adapter.in.web.security.AdminOnly
    public Mono<ResponseEntity<Void>> deleteWordBank(@PathVariable UUID id) {
        return repository.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    private WordBankResponse mapToResponse(WordBank entity) {
        return new WordBankResponse(entity.id(), entity.word(), entity.partsOfSpeech(), entity.chineseTranslation(), entity.targetLevel().name(), entity.difficultyLevel(), entity.examFrequency());
    }
}
