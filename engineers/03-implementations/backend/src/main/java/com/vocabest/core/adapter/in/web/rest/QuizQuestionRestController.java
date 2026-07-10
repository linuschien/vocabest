package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionRequest;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionResponse;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.repository.QuizQuestionRepository;
import com.vocabest.core.adapter.in.web.security.AdminOnly;
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
@RequestMapping("/api/v1/wordBanks/{wordBankId}/quizQuestions")
@AdminOnly
public class QuizQuestionRestController {

    private final QuizQuestionRepository repository;

    public QuizQuestionRestController(QuizQuestionRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Mono<ResponseEntity<QuizQuestionResponse>> createQuizQuestion(@PathVariable UUID wordBankId, @Valid @RequestBody QuizQuestionRequest req) {
        QuizQuestion entity = new QuizQuestion(null, wordBankId, req.contextualCloze(), req.chineseTranslation(), req.correctAnswer(), req.distractor1(), req.distractor2(), req.distractor3(), req.explanationRootAffix(), req.explanationMnemonic(), null, null, null);
        return repository.save(entity)
                .map(this::mapToResponse)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<QuizQuestionResponse>> getQuizQuestionById(@PathVariable UUID wordBankId, @PathVariable UUID id) {
        return repository.findById(id)
                .filter(q -> q.wordBankId().equals(wordBankId))
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<QuizQuestionResponse>> updateQuizQuestion(@PathVariable UUID wordBankId, @PathVariable UUID id, @Valid @RequestBody QuizQuestionRequest req) {
        return repository.findById(id)
                .filter(q -> q.wordBankId().equals(wordBankId))
                .map(existing -> new QuizQuestion(existing.id(), wordBankId, req.contextualCloze(), req.chineseTranslation(), req.correctAnswer(), req.distractor1(), req.distractor2(), req.distractor3(), req.explanationRootAffix(), req.explanationMnemonic(), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(repository::save)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteQuizQuestion(@PathVariable UUID wordBankId, @PathVariable UUID id) {
        return repository.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    private QuizQuestionResponse mapToResponse(QuizQuestion entity) {
        return new QuizQuestionResponse(entity.id(), entity.wordBankId().toString(), entity.contextualCloze(), entity.chineseTranslation(), entity.correctAnswer(), entity.distractor1(), entity.distractor2(), entity.distractor3(), entity.explanationRootAffix(), entity.explanationMnemonic());
    }
}
