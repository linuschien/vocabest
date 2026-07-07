package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.ErrorEventFilterInput;
import com.vocabest.core.adapter.out.persistence.model.ErrorEvent;
import com.vocabest.core.adapter.out.persistence.repository.ErrorEventRepository;
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
class ErrorEventGraphQLResolverTest {

    @Mock
    private ErrorEventRepository repository;

    @InjectMocks
    private ErrorEventGraphQLResolver resolver;

    @Test
    void testListErrorEvents() {
        UUID wordId = UUID.randomUUID();
        ErrorEvent entity = new ErrorEvent(UUID.randomUUID(), UUID.randomUUID(), wordId, LocalDateTime.now(), "dist", LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findAll(any(Example.class))).thenReturn(Flux.just(entity));

        StepVerifier.create(resolver.listErrorEvents(new ErrorEventFilterInput(UUID.randomUUID())))
                .expectNextMatches(e -> e.quizQuestionId().equals(wordId))
                .verifyComplete();
    }
}
