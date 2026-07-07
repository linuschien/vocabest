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
@com.vocabest.core.adapter.in.web.security.AdminOnly
public class DailyProgressGraphQLResolver {

    private final DailyProgressRepository repository;

    public DailyProgressGraphQLResolver(DailyProgressRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    public Flux<DailyProgress> listDailyProgresses(@Argument DailyProgressFilterInput filter) {
        if (filter == null || filter.userId() == null) {
            return Flux.error(new IllegalArgumentException("Filter is required to prevent unauthorized data access"));
        }
        DailyProgress probe = new DailyProgress(null, filter.userId(), null, null, null, null, null, null, null, null);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues();
        return repository.findAll(Example.of(probe, matcher));
    }
}
