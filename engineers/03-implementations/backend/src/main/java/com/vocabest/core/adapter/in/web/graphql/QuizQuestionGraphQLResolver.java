package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionFilterInput;
import com.vocabest.core.adapter.in.web.dto.QuizQuestionResponse;
import com.vocabest.core.application.port.in.QuizQuestionQueryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class QuizQuestionGraphQLResolver {

    private final QuizQuestionQueryService queryService;

    public QuizQuestionGraphQLResolver(QuizQuestionQueryService queryService) {
        this.queryService = queryService;
    }

    @QueryMapping
    public Flux<QuizQuestionResponse> listQuizQuestions(@Argument QuizQuestionFilterInput filter) {
        return queryService.listQuizQuestions(filter);
    }
}
