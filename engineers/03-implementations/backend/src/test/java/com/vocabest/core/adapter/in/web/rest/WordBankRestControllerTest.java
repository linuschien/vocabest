package com.vocabest.core.adapter.in.web.rest;


import com.vocabest.core.adapter.in.web.dto.WordBankRequest;
import com.vocabest.core.adapter.in.web.dto.WordBankResponse;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import com.vocabest.core.adapter.out.persistence.repository.WordBankRepository;
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
class WordBankRestControllerTest {

    @Mock
    private WordBankRepository repository;

    @InjectMocks
    private WordBankRestController controller;

    private WebTestClient webTestClient;

    private WordBank testWordBank;
    private UUID testId;

    @BeforeEach
    void setUp() {
        webTestClient = WebTestClient.bindToController(controller).build();
        testId = UUID.randomUUID();
        testWordBank = new WordBank(testId, "test", "noun", "測試", TargetLevel.JUNIOR_HIGH, 1, 5, LocalDateTime.now(), LocalDateTime.now(), null);
    }

    @Test
    void createWordBank_shouldReturnCreated() {
        WordBankRequest request = new WordBankRequest("test", "noun", "測試", "JUNIOR_HIGH", 1, 5);
        when(repository.save(any(WordBank.class))).thenReturn(Mono.just(testWordBank));

        webTestClient.post()
                .uri("/api/v1/word-banks")
                .bodyValue(request)
                .exchange()
                .expectStatus().isCreated()
                .expectBody(WordBankResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(testId, response.id()));
    }

    @Test
    void getWordBankById_shouldReturnOk() {
        when(repository.findById(testId)).thenReturn(Mono.just(testWordBank));

        webTestClient.get()
                .uri("/api/v1/word-banks/{id}", testId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WordBankResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals(testId, response.id()));
    }

    @Test
    void updateWordBank_shouldReturnOk() {
        WordBankRequest request = new WordBankRequest("test2", "noun", "測試2", "JUNIOR_HIGH", 2, 4);
        WordBank updatedWordBank = new WordBank(testId, "test2", "noun", "測試2", TargetLevel.JUNIOR_HIGH, 2, 4, LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findById(testId)).thenReturn(Mono.just(testWordBank));
        when(repository.save(any(WordBank.class))).thenReturn(Mono.just(updatedWordBank));

        webTestClient.put()
                .uri("/api/v1/word-banks/{id}", testId)
                .bodyValue(request)
                .exchange()
                .expectStatus().isOk()
                .expectBody(WordBankResponse.class)
                .value(response -> org.junit.jupiter.api.Assertions.assertEquals("test2", response.word()));
    }

    @Test
    void deleteWordBank_shouldReturnNoContent() {
        when(repository.deleteById(testId)).thenReturn(Mono.empty());

        webTestClient.delete()
                .uri("/api/v1/word-banks/{id}", testId)
                .exchange()
                .expectStatus().isNoContent();
    }
}
