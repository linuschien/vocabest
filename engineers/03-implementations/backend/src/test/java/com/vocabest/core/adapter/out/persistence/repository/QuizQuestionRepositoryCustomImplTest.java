package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionFilterInput;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.r2dbc.core.RowsFetchSpec;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.util.UUID;
import java.util.function.BiFunction;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QuizQuestionRepositoryCustomImplTest {

    @Mock
    private DatabaseClient databaseClient;

    @Mock
    private DatabaseClient.GenericExecuteSpec executeSpec;

    @Mock
    private RowsFetchSpec<QuizQuestion> quizQuestionRowsFetchSpec;

    @Mock
    private RowsFetchSpec<Long> longRowsFetchSpec;

    @InjectMocks
    private QuizQuestionRepositoryCustomImpl repository;

    private QuizQuestion testQuestion;
    private Long testCount;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        testQuestion = new QuizQuestion(UUID.randomUUID(), UUID.randomUUID(), "test", "test", "test", "t1", "t2", "t3", "root", "mnem", null, null, null);
        testCount = 5L;

        when(databaseClient.sql(anyString())).thenReturn(executeSpec);
        org.mockito.Mockito.lenient().when(executeSpec.bind(anyInt(), any())).thenReturn(executeSpec);
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_shouldReturnFluxOfQuestions() {
        QuizQuestionFilterInput filter = new QuizQuestionFilterInput("word", "A", 1, "JUNIOR_HIGH", 0, 10);
        
        when(executeSpec.map(any(BiFunction.class))).thenReturn(quizQuestionRowsFetchSpec);
        when(quizQuestionRowsFetchSpec.all()).thenReturn(Flux.just(testQuestion));

        StepVerifier.create(repository.search(filter))
                .expectNext(testQuestion)
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void search_withNullFilter_shouldReturnFluxOfQuestions() {
        when(executeSpec.map(any(BiFunction.class))).thenReturn(quizQuestionRowsFetchSpec);
        when(quizQuestionRowsFetchSpec.all()).thenReturn(Flux.just(testQuestion));

        StepVerifier.create(repository.search(null))
                .expectNext(testQuestion)
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void count_shouldReturnMonoOfLong() {
        QuizQuestionFilterInput filter = new QuizQuestionFilterInput("word", "A", 1, "JUNIOR_HIGH", 0, 10);
        
        when(executeSpec.map(any(BiFunction.class))).thenReturn(longRowsFetchSpec);
        when(longRowsFetchSpec.one()).thenReturn(Mono.just(testCount));

        StepVerifier.create(repository.count(filter))
                .expectNext(testCount)
                .verifyComplete();
    }

    @Test
    @SuppressWarnings("unchecked")
    void count_withNullFilter_shouldReturnMonoOfLong() {
        when(executeSpec.map(any(BiFunction.class))).thenReturn(longRowsFetchSpec);
        when(longRowsFetchSpec.one()).thenReturn(Mono.just(testCount));

        StepVerifier.create(repository.count(null))
                .expectNext(testCount)
                .verifyComplete();
    }
}
