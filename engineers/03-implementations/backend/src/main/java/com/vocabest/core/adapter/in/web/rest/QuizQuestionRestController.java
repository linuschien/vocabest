package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionRequest;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionResponse;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionActionRequest;
import com.vocabest.core.adapter.in.web.dto.OperationStatus;
import com.vocabest.core.application.port.in.QuizQuestionCommandService;
import com.vocabest.core.application.port.in.QuizQuestionQueryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/quiz-questions")
public class QuizQuestionRestController {

    private final QuizQuestionCommandService commandService;
    private final QuizQuestionQueryService queryService;

    public QuizQuestionRestController(QuizQuestionCommandService commandService, QuizQuestionQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping
    public Mono<ResponseEntity<QuizQuestionResponse>> createQuizQuestion(@RequestBody QuizQuestionRequest req) {
        return commandService.createQuizQuestion(req)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<QuizQuestionResponse>> getQuizQuestionById(@PathVariable UUID id) {
        return queryService.getQuizQuestionById(id)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<QuizQuestionResponse>> updateQuizQuestion(@PathVariable UUID id, @RequestBody QuizQuestionRequest req) {
        return commandService.updateQuizQuestion(id, req)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteQuizQuestion(@PathVariable UUID id) {
        return commandService.deleteQuizQuestion(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/generateBatch")
    public Mono<ResponseEntity<OperationStatus>> generateBatch(@RequestBody QuizQuestionActionRequest req) {
        return commandService.generateBatch(req)
                .map(ResponseEntity::ok);
    }
}
