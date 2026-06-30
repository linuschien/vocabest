package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.ErrorLogRequest;
import com.vocabest.core.adapter.in.web.dto.ErrorLogResponse;
import com.vocabest.core.adapter.in.web.dto.ErrorLogActionRequest;
import com.vocabest.core.adapter.in.web.dto.OperationStatus;
import com.vocabest.core.application.port.in.ErrorLogCommandService;
import com.vocabest.core.application.port.in.ErrorLogQueryService;
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
class ErrorLogRestControllerTest {

    @Mock
    private ErrorLogCommandService commandService;
    @Mock
    private ErrorLogQueryService queryService;

    @InjectMocks
    private ErrorLogRestController controller;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToController(controller).build();
    }

    @Test
    void testCreateErrorLog() {
        UUID userId = UUID.randomUUID();
        UUID wordId = UUID.randomUUID();
        ErrorLogResponse res = new ErrorLogResponse(UUID.randomUUID(), wordId, 1, LocalDateTime.now());
        when(commandService.createErrorLog(any())).thenReturn(Mono.just(res));

        client.post().uri("/api/v1/users/{userId}/error-logs", userId)
                .bodyValue(new ErrorLogRequest(wordId, 1, LocalDateTime.now()))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void testGetErrorLogById() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ErrorLogResponse res = new ErrorLogResponse(id, UUID.randomUUID(), 1, LocalDateTime.now());
        when(queryService.getErrorLogById(id)).thenReturn(Mono.just(res));

        client.get().uri("/api/v1/users/{userId}/error-logs/{id}", userId, id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateErrorLog() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        ErrorLogResponse res = new ErrorLogResponse(id, UUID.randomUUID(), 1, LocalDateTime.now());
        when(commandService.updateErrorLog(any(), any())).thenReturn(Mono.just(res));

        client.put().uri("/api/v1/users/{userId}/error-logs/{id}", userId, id)
                .bodyValue(new ErrorLogRequest(UUID.randomUUID(), 1, LocalDateTime.now()))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testDeleteErrorLog() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        when(commandService.deleteErrorLog(id)).thenReturn(Mono.empty());

        client.delete().uri("/api/v1/users/{userId}/error-logs/{id}", userId, id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testRecordFailure() {
        UUID userId = UUID.randomUUID();
        UUID id = UUID.randomUUID();
        when(commandService.recordFailure(any(), any())).thenReturn(Mono.just(new OperationStatus(true, "ok")));

        client.post().uri("/api/v1/users/{userId}/error-logs/{id}:recordFailure", userId, id)
                .bodyValue(new ErrorLogActionRequest("reason"))
                .exchange()
                .expectStatus().isOk();
    }
}
