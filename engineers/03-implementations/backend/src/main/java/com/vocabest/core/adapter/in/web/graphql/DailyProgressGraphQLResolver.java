package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.DailyProgressFilterInput;
import com.vocabest.core.adapter.out.persistence.model.DailyProgress;
import com.vocabest.core.adapter.out.persistence.repository.DailyProgressRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class DailyProgressGraphQLResolver {

    private final DailyProgressRepository repository;

    public DailyProgressGraphQLResolver(DailyProgressRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    @com.vocabest.core.adapter.in.web.security.RequireOwnership("#filter?.userId()")
    public Flux<DailyProgress> listDailyProgresses(@Argument DailyProgressFilterInput filter) {
        java.time.LocalDate filterDate = null;
        if (filter != null && filter.date() != null) {
            filterDate = java.time.LocalDate.parse(filter.date());
        }
        DailyProgress probe = new DailyProgress(
            filter != null ? filter.id() : null, 
            filter != null ? filter.userId() : null, 
            filterDate, 
            filter != null ? filter.targetQuestions() : null, 
            filter != null ? filter.answeredQuestions() : null, 
            filter != null ? filter.correctQuestions() : null, 
            filter != null ? filter.wrongQuestions() : null, 
            null, null, null);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues();
        return repository.findAll(Example.of(probe, matcher));
    }
}
