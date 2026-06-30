package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.DailyProgressRequest;
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

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToController(controller).build();
    }

    @Test
    void testCreateDailyProgress() {
        UUID userId = UUID.randomUUID();
        DailyProgress dp = new DailyProgress(UUID.randomUUID(), userId, LocalDateTime.now(), 10, LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.save(any())).thenReturn(Mono.just(dp));

        client.post().uri("/api/v1/users/{userId}/daily-progresses", userId)
                .bodyValue(new DailyProgressRequest(LocalDateTime.now(), 10))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.id").isNotEmpty();
    }

    @Test
    void testGetDailyProgressById() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        DailyProgress dp = new DailyProgress(id, userId, LocalDateTime.now(), 10, LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findById(id)).thenReturn(Mono.just(dp));

        client.get().uri("/api/v1/users/{userId}/daily-progresses/{id}", userId, id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateDailyProgress() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        DailyProgress dp = new DailyProgress(id, userId, LocalDateTime.now(), 10, LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findById(id)).thenReturn(Mono.just(dp));
        when(repository.save(any())).thenReturn(Mono.just(dp));

        client.put().uri("/api/v1/users/{userId}/daily-progresses/{id}", userId, id)
                .bodyValue(new DailyProgressRequest(LocalDateTime.now(), 10))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testDeleteDailyProgress() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        DailyProgress dp = new DailyProgress(id, userId, LocalDateTime.now(), 10, LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findById(id)).thenReturn(Mono.just(dp));
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        client.delete().uri("/api/v1/users/{userId}/daily-progresses/{id}", userId, id)
                .exchange()
                .expectStatus().isNoContent();
    }
}
