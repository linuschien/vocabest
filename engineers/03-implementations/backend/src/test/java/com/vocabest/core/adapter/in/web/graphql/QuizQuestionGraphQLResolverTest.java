package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionFilterInput;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionResponse;
import com.vocabest.core.application.port.in.QuizQuestionQueryService;
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
class QuizQuestionGraphQLResolverTest {

    @Mock
    private QuizQuestionQueryService queryService;

    @InjectMocks
    private QuizQuestionGraphQLResolver resolver;

    @Test
    void testListQuizQuestions() {
        QuizQuestionResponse res = new QuizQuestionResponse(UUID.randomUUID(), UUID.randomUUID().toString(), "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", "JUNIOR_HIGH");
        when(queryService.listQuizQuestions(any())).thenReturn(Flux.just(res));

        StepVerifier.create(resolver.listQuizQuestions(new QuizQuestionFilterInput("JUNIOR_HIGH")))
                .expectNextMatches(e -> e.contextualCloze().equals("cloze"))
                .verifyComplete();
    }
}
