package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionRequest;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionResponse;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import com.vocabest.core.adapter.out.persistence.repository.QuizQuestionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizQuestionRestControllerTest {

    @Mock
    private QuizQuestionRepository repository;

    @InjectMocks
    private QuizQuestionRestController controller;

    private WebTestClient webTestClient;

    private QuizQuestion testQuizQuestion;
    private UUID testId;
    private UUID testWordBankId;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(controller).build();
        testId = UUID.randomUUID();
        testWordBankId = UUID.randomUUID();
        testQuizQuestion = new QuizQuestion(testId, testWordBankId, "context", "chinese", "correct", "d1", "d2", "d3", "root", "mnem", LocalDateTime.now(), LocalDateTime.now(), null);
    }

    @Test
    void createQuizQuestion_shouldReturnCreated() {
        QuizQuestionRequest request = new QuizQuestionRequest("context", "chinese", "correct", "d1", "d2", "d3", "root", "mnem");
        when(repository.save(any(QuizQuestion.class))).thenReturn(Mono.just(testQuizQuestion));

        webTestClient.post()
                .uri("/api/v1/wordBanks/{wordBankId}/quizQuestions", testWordBankId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(QuizQuestionResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(testId, response.id()));
    }

    @Test
    void getQuizQuestionById_shouldReturnOk() {
        when(repository.findById(testId)).thenReturn(Mono.just(testQuizQuestion));

        webTestClient.get()
                .uri("/api/v1/wordBanks/{wordBankId}/quizQuestions/{id}", testWordBankId, testId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(QuizQuestionResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(testId, response.id()));
    }

    @Test
    void updateQuizQuestion_shouldReturnOk() {
        QuizQuestionRequest request = new QuizQuestionRequest("context2", "chinese", "correct", "d1", "d2", "d3", "root", "mnem");
        QuizQuestion updated = new QuizQuestion(testId, testWordBankId, "context2", "chinese", "correct", "d1", "d2", "d3", "root", "mnem", LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findById(testId)).thenReturn(Mono.just(testQuizQuestion));
        when(repository.save(any(QuizQuestion.class))).thenReturn(Mono.just(updated));

        webTestClient.put()
                .uri("/api/v1/wordBanks/{wordBankId}/quizQuestions/{id}", testWordBankId, testId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(QuizQuestionResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals("context2", response.contextualCloze()));
    }

    @Test
    void deleteQuizQuestion_shouldReturnNoContent() {
        when(repository.deleteById(testId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/wordBanks/{wordBankId}/quizQuestions/{id}", testWordBankId, testId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
