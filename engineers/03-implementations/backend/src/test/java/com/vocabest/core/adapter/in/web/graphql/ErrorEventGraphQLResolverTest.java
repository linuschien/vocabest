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
        com.vocabest.core.adapter.in.web.dto.ErrorEventPage page = new com.vocabest.core.adapter.in.web.dto.ErrorEventPage(java.util.List.of(entity), 1);
        when(repository.search(any())).thenReturn(reactor.core.publisher.Mono.just(page));

        UUID filterUserId = UUID.randomUUID();
        ErrorEventFilterInput filter = new ErrorEventFilterInput(null, filterUserId, null, null, null, null, null, null, null);
        StepVerifier.create(resolver.listErrorEvents(filter)
                .contextWrite(reactor.util.context.Context.of("CURRENT_USER", new com.vocabest.core.adapter.out.persistence.model.User(filterUserId, "test", com.vocabest.core.adapter.out.persistence.model.Role.LEARNER, null, 0, 0, 0, 0, null, null, null))))
                .expectNextMatches(p -> p.content().get(0).quizQuestionId().equals(wordId) && p.totalElements() == 1)
                .verifyComplete();
    }
}
