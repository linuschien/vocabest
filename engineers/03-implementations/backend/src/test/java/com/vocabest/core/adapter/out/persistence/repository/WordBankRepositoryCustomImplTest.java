package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.in.web.dto.WordBankFilterInput;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WordBankRepositoryCustomImplTest {

    @Mock
    private DatabaseClient databaseClient;

    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;

    @Mock
    private RowsFetchSpec<WordBank> wordBankRowsFetchSpec;

    @InjectMocks
    private WordBankRepositoryCustomImpl repository;

    private WordBank testWordBank;

    @Mock
    private RowsFetchSpec<Long> countFetchSpec;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        testWordBank = new WordBank(UUID.randomUUID(), "test", "noun", "測試", TargetLevel.JUNIOR_HIGH, 1, 10, null, null, null);

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        org.mockito.Mockito.lenient().when(executeSpec.bind(anyInt(), any())).thenReturn(executeSpec);
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_shouldReturnFluxOfWordBanks() {
        WordBankFilterInput filter = new WordBankFilterInput("test", "t", 1, "JUNIOR_HIGH", 0, 10);
        
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyInt(), any())).thenReturn(executeSpec);
        
        org.mockito.Mockito.doReturn(countFetchSpec, wordBankRowsFetchSpec).when(executeSpec).map(any(BiFunction.class));
        when(countFetchSpec.one()).thenReturn(reactor.core.publisher.Mono.just(1L));
        when(wordBankRowsFetchSpec.all()).thenReturn(Flux.just(testWordBank));

        StepVerifier.create(repository.search(filter))
                .expectNextMatches(page -> page.totalElements() == 1L && page.content().get(0).word().equals("test"))
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_withNullFilter_shouldReturnFluxOfWordBanks() {
        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        when(executeSpec.bind(anyInt(), any())).thenReturn(executeSpec);
        
        org.mockito.Mockito.doReturn(countFetchSpec, wordBankRowsFetchSpec).when(executeSpec).map(any(BiFunction.class));
        when(countFetchSpec.one()).thenReturn(reactor.core.publisher.Mono.just(1L));
        when(wordBankRowsFetchSpec.all()).thenReturn(Flux.just(testWordBank));

        StepVerifier.create(repository.search(null))
                .expectNextMatches(page -> page.totalElements() == 1L && page.content().get(0).word().equals("test"))
                .verifyComplete();
    }
}
