package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.VocabularyWordRequest;
import com.vocabest.core.adapter.in.web.dto.VocabularyWordResponse;
import com.vocabest.core.adapter.in.web.dto.VocabularyWordActionRequest;
import com.vocabest.core.adapter.in.web.dto.OperationStatus;
import com.vocabest.core.application.port.in.VocabularyWordCommandService;
import com.vocabest.core.application.port.in.VocabularyWordQueryService;
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
class VocabularyWordRestControllerTest {

    @Mock
    private VocabularyWordCommandService commandService;
    @Mock
    private VocabularyWordQueryService queryService;

    @InjectMocks
    private VocabularyWordRestController controller;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToController(controller).build();
    }

    @Test
    void testCreateVocabularyWord() {
        VocabularyWordResponse res = new VocabularyWordResponse(UUID.randomUUID(), "word", "verb", "trans", "JUNIOR_BASIC_1200", 1);
        when(commandService.createVocabularyWord(any())).thenReturn(Mono.just(res));

        client.post().uri("/api/v1/vocabulary-words")
                .bodyValue(new VocabularyWordRequest("word", "verb", "trans", "JUNIOR_BASIC_1200", 1))
                .exchange()
                .expectStatus().isCreated();
    }

    @Test
    void testGetVocabularyWordById() {
        UUID id = UUID.randomUUID();
        VocabularyWordResponse res = new VocabularyWordResponse(id, "word", "verb", "trans", "JUNIOR_BASIC_1200", 1);
        when(queryService.getVocabularyWordById(id)).thenReturn(Mono.just(res));

        client.get().uri("/api/v1/vocabulary-words/{id}", id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateVocabularyWord() {
        UUID id = UUID.randomUUID();
        VocabularyWordResponse res = new VocabularyWordResponse(id, "word", "verb", "trans", "JUNIOR_BASIC_1200", 1);
        when(commandService.updateVocabularyWord(any(), any())).thenReturn(Mono.just(res));

        client.put().uri("/api/v1/vocabulary-words/{id}", id)
                .bodyValue(new VocabularyWordRequest("word", "verb", "trans", "JUNIOR_BASIC_1200", 1))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testDeleteVocabularyWord() {
        UUID id = UUID.randomUUID();
        when(commandService.deleteVocabularyWord(id)).thenReturn(Mono.empty());

        client.delete().uri("/api/v1/vocabulary-words/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testImportBulk() {
        when(commandService.importBulk(any())).thenReturn(Mono.just(new OperationStatus(true, "ok")));

        client.post().uri("/api/v1/vocabulary-words/importBulk")
                .bodyValue(new VocabularyWordActionRequest("file.csv"))
                .exchange()
                .expectStatus().isOk();
    }
}
