package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.WordMasteryFilterInput;
import com.vocabest.core.adapter.out.persistence.model.WordMastery;
import com.vocabest.core.adapter.out.persistence.repository.WordMasteryRepository;
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
class WordMasteryGraphQLResolverTest {

    @Mock
    private WordMasteryRepository repository;

    @InjectMocks
    private WordMasteryGraphQLResolver resolver;

    @Test
    void testListWordMasteries() {
        UUID userId = UUID.randomUUID();
        WordMastery entity = new WordMastery(UUID.randomUUID(), userId, UUID.randomUUID(), 1, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findAll(any(Example.class))).thenReturn(Flux.just(entity));

        StepVerifier.create(resolver.listWordMasteries(new WordMasteryFilterInput(null, userId, null, null))
                .contextWrite(reactor.util.context.Context.of("CURRENT_USER", new com.vocabest.core.adapter.out.persistence.model.User(userId, "test", com.vocabest.core.adapter.out.persistence.model.Role.LEARNER, null, 0, 0, 0, 0, null, null, null))))
                .expectNextMatches(e -> e.userId().equals(userId))
                .verifyComplete();
    }
}
