package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.WordMasteryRequest;
import com.vocabest.core.adapter.in.web.dto.WordMasteryResponse;
import com.vocabest.core.adapter.out.persistence.model.WordMastery;
import com.vocabest.core.adapter.out.persistence.repository.WordMasteryRepository;
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
class WordMasteryRestControllerTest {

    @Mock
    private WordMasteryRepository repository;

    @InjectMocks
    private WordMasteryRestController controller;

    private WebTestClient webTestClient;

    private WordMastery testWordMastery;
    private UUID testId;
    private UUID testUserId;
    private UUID testWordBankId;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(controller).build();
        testId = UUID.randomUUID();
        testUserId = UUID.randomUUID();
        testWordBankId = UUID.randomUUID();
        testWordMastery = new WordMastery(testId, testUserId, testWordBankId, 1, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
    }

    @Test
    void createWordMastery_shouldReturnCreated() {
        WordMasteryRequest request = new WordMasteryRequest(testWordBankId.toString(), 1, LocalDateTime.now());
        when(repository.save(any(WordMastery.class))).thenReturn(Mono.just(testWordMastery));

        webTestClient.post()
                .uri("/api/v1/users/{userId}/wordMasteries", testUserId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(WordMasteryResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(testId, response.id()));
    }

    @Test
    void getWordMasteryById_shouldReturnOk() {
        when(repository.findById(testId)).thenReturn(Mono.just(testWordMastery));

        webTestClient.get()
                .uri("/api/v1/users/{userId}/wordMasteries/{id}", testUserId, testId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WordMasteryResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(testId, response.id()));
    }

    @Test
    void updateWordMastery_shouldReturnOk() {
        WordMasteryRequest request = new WordMasteryRequest(testWordBankId.toString(), 2, LocalDateTime.now());
        WordMastery updated = new WordMastery(testId, testUserId, testWordBankId, 2, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findById(testId)).thenReturn(Mono.just(testWordMastery));
        when(repository.save(any(WordMastery.class))).thenReturn(Mono.just(updated));

        webTestClient.put()
                .uri("/api/v1/users/{userId}/wordMasteries/{id}", testUserId, testId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WordMasteryResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(2, response.errorWeight()));
    }

    @Test
    void deleteWordMastery_shouldReturnNoContent() {
        when(repository.deleteById(testId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/users/{userId}/wordMasteries/{id}", testUserId, testId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
