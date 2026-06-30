package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.ErrorLogFilterInput;
import com.vocabest.core.adapter.in.web.dto.ErrorLogResponse;
import com.vocabest.core.application.port.in.ErrorLogQueryService;
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
class ErrorLogGraphQLResolverTest {

    @Mock
    private ErrorLogQueryService queryService;

    @InjectMocks
    private ErrorLogGraphQLResolver resolver;

    @Test
    void testListErrorLogs() {
        UUID wordId = UUID.randomUUID();
        ErrorLogResponse res = new ErrorLogResponse(UUID.randomUUID(), wordId, 1, LocalDateTime.now());
        when(queryService.listErrorLogs(any())).thenReturn(Flux.just(res));

        StepVerifier.create(resolver.listErrorLogs(new ErrorLogFilterInput(UUID.randomUUID())))
                .expectNextMatches(e -> e.wordId().equals(wordId))
                .verifyComplete();
    }
}
