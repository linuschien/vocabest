package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.DailyProgressFilterInput;
import com.vocabest.core.adapter.out.persistence.model.DailyProgress;
import com.vocabest.core.adapter.out.persistence.repository.DailyProgressRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DailyProgressGraphQLResolverTest {

    @Mock
    private DailyProgressRepository repository;

    @InjectMocks
    private DailyProgressGraphQLResolver resolver;

    @Test
    void testListDailyProgresses() {
        StepVerifier.create(resolver.listDailyProgresses(null))
                .expectError(IllegalArgumentException.class)
                .verify();
    }

    @Test
    void testListDailyProgressesWithFilter() {
        UUID userId = UUID.randomUUID();
        DailyProgress dp = new DailyProgress(UUID.randomUUID(), userId, LocalDateTime.now(), 10, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.domain.Example<DailyProgress>>any())).thenReturn(Flux.just(dp));

        StepVerifier.create(resolver.listDailyProgresses(new DailyProgressFilterInput(userId)))
                .expectNextMatches(e -> e.userId().equals(userId))
                .verifyComplete();
    }
}
