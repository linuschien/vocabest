package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.DailyProgressRequest;
import com.vocabest.core.adapter.in.web.dto.DailyProgressResponse;
import com.vocabest.core.adapter.out.persistence.model.DailyProgress;
import com.vocabest.core.adapter.out.persistence.repository.DailyProgressRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyProgressRestControllerTest {

    @Mock
    private DailyProgressRepository repository;

    @InjectMocks
    private DailyProgressRestController controller;

    private WebTestClient webTestClient;

    private DailyProgress testDailyProgress;
    private UUID testId;
    private UUID testUserId;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(controller).build();
        testId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testDailyProgress = new DailyProgress(testId, testUserId, LocalDate.now(), 20, 10, 8, 2, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    @Test
    void createDailyProgress_shouldReturnCreated() {
        DailyProgressRequest request = new DailyProgressRequest(LocalDate.now(), 20, 10, 8, 2);
        when(repository.save(any(DailyProgress.class))).thenReturn(Mono.just(testDailyProgress));

        webTestClient.post()
                .uri("/api/v1/users/{userId}/daily-progresses", testUserId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(DailyProgressResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(testId, response.id()));
    }

    @Test
    void getDailyProgressById_shouldReturnOk() {
        when(repository.findById(testId)).thenReturn(Mono.just(testDailyProgress));

        webTestClient.get()
                .uri("/api/v1/users/{userId}/daily-progresses/{id}", testUserId, testId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DailyProgressResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(testId, response.id()));
    }

    @Test
    void updateDailyProgress_shouldReturnOk() {
        DailyProgressRequest request = new DailyProgressRequest(LocalDate.now(), 20, 15, 12, 3);
        DailyProgress updated = new DailyProgress(testId, testUserId, LocalDate.now(), 20, 15, 12, 3, LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findById(testId)).thenReturn(Mono.just(testDailyProgress));
        when(repository.save(any(DailyProgress.class))).thenReturn(Mono.just(updated));

        webTestClient.put()
                .uri("/api/v1/users/{userId}/daily-progresses/{id}", testUserId, testId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(DailyProgressResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(15, response.answeredQuestions()));
    }

    @Test
    void deleteDailyProgress_shouldReturnNoContent() {
        when(repository.findById(testId)).thenReturn(Mono.just(testDailyProgress));
        when(repository.deleteById(testId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/users/{userId}/daily-progresses/{id}", testUserId, testId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
