package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.ErrorEventRequest;
import com.vocabest.core.adapter.in.web.dto.ErrorEventResponse;
import com.vocabest.core.adapter.out.persistence.model.ErrorEvent;
import com.vocabest.core.adapter.out.persistence.repository.ErrorEventRepository;
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
class ErrorEventRestControllerTest {

    @Mock
    private ErrorEventRepository repository;

    @InjectMocks
    private ErrorEventRestController controller;

    private WebTestClient webTestClient;

    private ErrorEvent testErrorEvent;
    private UUID testId;
    private UUID testUserId;
    private UUID testQuestionId;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(controller).build();
        testId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testQuestionId = UUID.randomUUID();
        testErrorEvent = new ErrorEvent(testId, testUserId, testQuestionId, LocalDateTime.now(), "wrong_answer", LocalDateTime.now(), LocalDateTime.now(), null);
    }

    @Test
    void createErrorEvent_shouldReturnCreated() {
        ErrorEventRequest request = new ErrorEventRequest(testQuestionId, LocalDateTime.now(), "wrong_answer");
        when(repository.save(any(ErrorEvent.class))).thenReturn(Mono.just(testErrorEvent));

        webTestClient.post()
                .uri("/api/v1/users/{userId}/errorEvents", testUserId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(ErrorEventResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(testId, response.id()));
    }

    @Test
    void getErrorEventById_shouldReturnOk() {
        when(repository.findById(testId)).thenReturn(Mono.just(testErrorEvent));

        webTestClient.get()
                .uri("/api/v1/users/{userId}/errorEvents/{id}", testUserId, testId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ErrorEventResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(testId, response.id()));
    }

    @Test
    void updateErrorEvent_shouldReturnOk() {
        ErrorEventRequest request = new ErrorEventRequest(testQuestionId, LocalDateTime.now(), "wrong_answer_2");
        ErrorEvent updated = new ErrorEvent(testId, testUserId, testQuestionId, LocalDateTime.now(), "wrong_answer_2", LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findById(testId)).thenReturn(Mono.just(testErrorEvent));
        when(repository.save(any(ErrorEvent.class))).thenReturn(Mono.just(updated));

        webTestClient.put()
                .uri("/api/v1/users/{userId}/errorEvents/{id}", testUserId, testId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(ErrorEventResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals("wrong_answer_2", response.selectedDistractor()));
    }

    @Test
    void deleteErrorEvent_shouldReturnNoContent() {
        when(repository.deleteById(testId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/users/{userId}/errorEvents/{id}", testUserId, testId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
