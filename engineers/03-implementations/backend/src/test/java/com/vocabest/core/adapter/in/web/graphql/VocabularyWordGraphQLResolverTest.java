package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.VocabularyWordFilterInput;
import com.vocabest.core.adapter.in.web.dto.VocabularyWordResponse;
import com.vocabest.core.application.port.in.VocabularyWordQueryService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VocabularyWordGraphQLResolverTest {

    @Mock
    private VocabularyWordQueryService queryService;

    @InjectMocks
    private VocabularyWordGraphQLResolver resolver;

    @Test
    void testListVocabularyWords() {
        VocabularyWordResponse res = new VocabularyWordResponse(UUID.randomUUID(), "word", "verb", "trans", "JUNIOR_BASIC_1200", 1);
        when(queryService.listVocabularyWords(any())).thenReturn(Flux.just(res));

        StepVerifier.create(resolver.listVocabularyWords(new VocabularyWordFilterInput("JUNIOR_BASIC_1200")))
                .expectNextMatches(e -> e.word().equals("word"))
                .verifyComplete();
    }
}
