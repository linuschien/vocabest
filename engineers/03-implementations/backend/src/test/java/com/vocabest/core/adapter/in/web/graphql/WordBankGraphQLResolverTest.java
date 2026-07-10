package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.WordBankFilterInput;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import com.vocabest.core.adapter.out.persistence.repository.WordBankRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordBankGraphQLResolverTest {

    @Mock
    private WordBankRepository repository;

    @InjectMocks
    private WordBankGraphQLResolver resolver;

    @Test
    void testListWordBanks() {
        WordBank entity = new WordBank(UUID.randomUUID(), "word", "verb", "trans", TargetLevel.JUNIOR_HIGH, 1, 5, LocalDateTime.now(), LocalDateTime.now(), null);
        com.vocabest.core.adapter.in.web.dto.WordBankPage page = new com.vocabest.core.adapter.in.web.dto.WordBankPage(java.util.List.of(entity), 1L);
        when(repository.search(any())).thenReturn(reactor.core.publisher.Mono.just(page));

        StepVerifier.create(resolver.listWordBanks(new WordBankFilterInput("word", "w", 1, "JUNIOR_HIGH", 0, 20)))
                .expectNextMatches(e -> e.content().get(0).word().equals("word") && e.totalElements() == 1L)
                .verifyComplete();
    }
}
