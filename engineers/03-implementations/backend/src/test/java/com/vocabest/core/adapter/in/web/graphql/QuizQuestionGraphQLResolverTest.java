package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionFilterInput;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import com.vocabest.core.adapter.out.persistence.repository.QuizQuestionRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizQuestionGraphQLResolverTest {

    @Mock
    private QuizQuestionRepository repository;

    @InjectMocks
    private QuizQuestionGraphQLResolver resolver;

    @Test
    void testListQuizQuestions() {
        QuizQuestion entity = new QuizQuestion(UUID.randomUUID(), UUID.randomUUID(), "cloze", "trans", "opt", "d1", "d2", "d3", "root", "mnem", LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.search(any())).thenReturn(Flux.just(entity));

        QuizQuestionFilterInput filter = new QuizQuestionFilterInput(
                null, "apple", "a", 1, "JUNIOR_HIGH", 0, 20
        );
        StepVerifier.create(resolver.listQuizQuestions(filter))
                .expectNextMatches(e -> e.contextualCloze().equals("cloze"))
                .verifyComplete();
    }
}
