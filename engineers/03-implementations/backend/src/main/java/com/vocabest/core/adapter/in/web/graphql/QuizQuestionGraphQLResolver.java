package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionFilterInput;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import com.vocabest.core.adapter.out.persistence.repository.QuizQuestionRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class QuizQuestionGraphQLResolver {

    private final QuizQuestionRepository repository;

    public QuizQuestionGraphQLResolver(QuizQuestionRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    public Flux<QuizQuestion> listQuizQuestions(@Argument QuizQuestionFilterInput filter) {
        return repository.search(filter);
    }
}
