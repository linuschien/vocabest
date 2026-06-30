package com.vocabest.core.application.service;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionRequest;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionActionRequest;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.repository.QuizQuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizQuestionServiceImplTest {

    @Mock
    private QuizQuestionRepository repository;

    @InjectMocks
    private QuizQuestionServiceImpl service;

    @Test
    void testCreateQuizQuestion() {
        UUID wordId = UUID.randomUUID();
        QuizQuestionRequest req = new QuizQuestionRequest(wordId.toString(), "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", "JUNIOR_HIGH");
        QuizQuestion entity = new QuizQuestion(UUID.randomUUID(), wordId, "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", TargetLevel.JUNIOR_HIGH, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.save(any())).thenReturn(Mono.just(entity));

        StepVerifier.create(service.createQuizQuestion(req))
                .expectNextMatches(res -> res.id() != null && res.wordId().equals(wordId.toString()))
                .verifyComplete();
    }

    @Test
    void testGetQuizQuestionById() {
        UUID id = UUID.randomUUID();
        UUID wordId = UUID.randomUUID();
        QuizQuestion entity = new QuizQuestion(id, wordId, "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", TargetLevel.JUNIOR_HIGH, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.findById(id)).thenReturn(Mono.just(entity));

        StepVerifier.create(service.getQuizQuestionById(id))
                .expectNextMatches(res -> res.id().equals(id))
                .verifyComplete();
    }

    @Test
    void testUpdateQuizQuestion() {
        UUID id = UUID.randomUUID();
        UUID wordId = UUID.randomUUID();
        QuizQuestionRequest req = new QuizQuestionRequest(wordId.toString(), "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", "JUNIOR_HIGH");
        QuizQuestion entity = new QuizQuestion(id, wordId, "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", TargetLevel.JUNIOR_HIGH, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.findById(id)).thenReturn(Mono.just(entity));
        when(repository.save(any())).thenReturn(Mono.just(entity));

        StepVerifier.create(service.updateQuizQuestion(id, req))
                .expectNextMatches(res -> res.id().equals(id))
                .verifyComplete();
    }

    @Test
    void testDeleteQuizQuestion() {
        UUID id = UUID.randomUUID();
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteQuizQuestion(id))
                .verifyComplete();
    }

    @Test
    void testListQuizQuestions() {
        QuizQuestion entity = new QuizQuestion(UUID.randomUUID(), UUID.randomUUID(), "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", TargetLevel.JUNIOR_HIGH, LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findAll()).thenReturn(Flux.just(entity));

        StepVerifier.create(service.listQuizQuestions(null))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testGenerateBatch() {
        StepVerifier.create(service.generateBatch(new QuizQuestionActionRequest(10, "JUNIOR_HIGH")))
                .expectNextMatches(res -> res.success())
                .verifyComplete();
    }
}
