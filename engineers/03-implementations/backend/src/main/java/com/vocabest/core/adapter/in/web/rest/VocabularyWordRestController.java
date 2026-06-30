package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.VocabularyWordRequest;
import com.vocabest.core.adapter.in.web.dto.VocabularyWordResponse;
import com.vocabest.core.adapter.in.web.dto.VocabularyWordActionRequest;
import com.vocabest.core.adapter.in.web.dto.OperationStatus;
import com.vocabest.core.application.port.in.VocabularyWordCommandService;
import com.vocabest.core.application.port.in.VocabularyWordQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/vocabulary-words")
public class VocabularyWordRestController {

    private final VocabularyWordCommandService commandService;
    private final VocabularyWordQueryService queryService;

    public VocabularyWordRestController(VocabularyWordCommandService commandService, VocabularyWordQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public Mono<ResponseEntity<VocabularyWordResponse>> createVocabularyWord(@RequestBody VocabularyWordRequest req) {
        return commandService.createVocabularyWord(req)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<VocabularyWordResponse>> getVocabularyWordById(@PathVariable UUID id) {
        return queryService.getVocabularyWordById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<VocabularyWordResponse>> updateVocabularyWord(@PathVariable UUID id, @RequestBody VocabularyWordRequest req) {
        return commandService.updateVocabularyWord(id, req)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteVocabularyWord(@PathVariable UUID id) {
        return commandService.deleteVocabularyWord(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/importBulk")
    public Mono<ResponseEntity<OperationStatus>> importBulk(@RequestBody VocabularyWordActionRequest req) {
        return commandService.importBulk(req)
                .map(ResponseEntity::ok);
    }
}
