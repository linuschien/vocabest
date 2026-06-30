package com.vocabest.core.application.service;

import com.vocabest.core.adapter.in.web.dto.VocabularyWordRequest;
import com.vocabest.core.adapter.in.web.dto.VocabularyWordActionRequest;
import com.vocabest.core.adapter.out.persistence.model.VocabularyLevel;
import com.vocabest.core.adapter.out.persistence.model.VocabularyWord;
import com.vocabest.core.adapter.out.persistence.repository.VocabularyWordRepository;
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
class VocabularyWordServiceImplTest {

    @Mock
    private VocabularyWordRepository repository;

    @InjectMocks
    private VocabularyWordServiceImpl service;

    @Test
    void testCreateVocabularyWord() {
        VocabularyWordRequest req = new VocabularyWordRequest("word", "verb", "trans", "JUNIOR_BASIC_1200", 1);
        VocabularyWord entity = new VocabularyWord(UUID.randomUUID(), "word", "verb", "trans", VocabularyLevel.JUNIOR_BASIC_1200, 1, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.save(any())).thenReturn(Mono.just(entity));

        StepVerifier.create(service.createVocabularyWord(req))
                .expectNextMatches(res -> res.id() != null && res.word().equals("word"))
                .verifyComplete();
    }

    @Test
    void testGetVocabularyWordById() {
        UUID id = UUID.randomUUID();
        VocabularyWord entity = new VocabularyWord(id, "word", "verb", "trans", VocabularyLevel.JUNIOR_BASIC_1200, 1, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.findById(id)).thenReturn(Mono.just(entity));

        StepVerifier.create(service.getVocabularyWordById(id))
                .expectNextMatches(res -> res.id().equals(id))
                .verifyComplete();
    }

    @Test
    void testUpdateVocabularyWord() {
        UUID id = UUID.randomUUID();
        VocabularyWordRequest req = new VocabularyWordRequest("word", "verb", "trans", "JUNIOR_BASIC_1200", 1);
        VocabularyWord entity = new VocabularyWord(id, "word", "verb", "trans", VocabularyLevel.JUNIOR_BASIC_1200, 1, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.findById(id)).thenReturn(Mono.just(entity));
        when(repository.save(any())).thenReturn(Mono.just(entity));

        StepVerifier.create(service.updateVocabularyWord(id, req))
                .expectNextMatches(res -> res.id().equals(id))
                .verifyComplete();
    }

    @Test
    void testDeleteVocabularyWord() {
        UUID id = UUID.randomUUID();
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        StepVerifier.create(service.deleteVocabularyWord(id))
                .verifyComplete();
    }

    @Test
    void testListVocabularyWords() {
        VocabularyWord entity = new VocabularyWord(UUID.randomUUID(), "word", "verb", "trans", VocabularyLevel.JUNIOR_BASIC_1200, 1, LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findAll()).thenReturn(Flux.just(entity));

        StepVerifier.create(service.listVocabularyWords(null))
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    void testImportBulk() {
        StepVerifier.create(service.importBulk(new VocabularyWordActionRequest("file.csv")))
                .expectNextMatches(res -> res.success())
                .verifyComplete();
    }
}
