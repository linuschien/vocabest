package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionRequest;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionResponse;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionActionRequest;
import com.vocabest.core.adapter.in.web.dto.OperationStatus;
import com.vocabest.core.application.port.in.QuizQuestionCommandService;
import com.vocabest.core.application.port.in.QuizQuestionQueryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizQuestionRestControllerTest {

    @Mock
    private QuizQuestionCommandService commandService;
    @Mock
    private QuizQuestionQueryService queryService;

    @InjectMocks
    private QuizQuestionRestController controller;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToController(controller).build();
    }

    @Test
    void testCreateQuizQuestion() {
        QuizQuestionResponse res = new QuizQuestionResponse(UUID.randomUUID(), UUID.randomUUID().toString(), "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", "JUNIOR_HIGH");
        when(commandService.createQuizQuestion(any())).thenReturn(Mono.just(res));

        client.post().uri("/api/v1/quiz-questions")
                .bodyValue(new QuizQuestionRequest(UUID.randomUUID().toString(), "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", "JUNIOR_HIGH"))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void testGetQuizQuestionById() {
        UUID id = UUID.randomUUID();
        QuizQuestionResponse res = new QuizQuestionResponse(id, UUID.randomUUID().toString(), "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", "JUNIOR_HIGH");
        when(queryService.getQuizQuestionById(id)).thenReturn(Mono.just(res));

        client.get().uri("/api/v1/quiz-questions/{id}", id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateQuizQuestion() {
        UUID id = UUID.randomUUID();
        QuizQuestionResponse res = new QuizQuestionResponse(id, UUID.randomUUID().toString(), "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", "JUNIOR_HIGH");
        when(commandService.updateQuizQuestion(any(), any())).thenReturn(Mono.just(res));

        client.put().uri("/api/v1/quiz-questions/{id}", id)
                .bodyValue(new QuizQuestionRequest(UUID.randomUUID().toString(), "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", "JUNIOR_HIGH"))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testDeleteQuizQuestion() {
        UUID id = UUID.randomUUID();
        when(commandService.deleteQuizQuestion(id)).thenReturn(Mono.empty());

        client.delete().uri("/api/v1/quiz-questions/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testGenerateBatch() {
        when(commandService.generateBatch(any())).thenReturn(Mono.just(new OperationStatus(true, "ok")));

        client.post().uri("/api/v1/quiz-questions/generateBatch")
                .bodyValue(new QuizQuestionActionRequest(10, "JUNIOR_HIGH"))
                .exchange()
                .expectStatus().isOk();
    }
}
