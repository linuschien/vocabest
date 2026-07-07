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
    public Flux<DailyProgress> listDailyProgresses(@Argument DailyProgressFilterInput filter) {
        if (filter == null || filter.userId() == null) {
            return Flux.error(new IllegalArgumentException("Filter is required to prevent unauthorized data access"));
        }
        return Flux.deferContextual(ctx -> {
            com.vocabest.core.adapter.out.persistence.model.User currentUser = ctx.getOrDefault("CURRENT_USER", null);
            if (currentUser == null) {
                return Flux.error(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthenticated"));
            }
            if (currentUser.role() != com.vocabest.core.adapter.out.persistence.model.Role.ADMIN && !currentUser.id().equals(filter.userId())) {
                return Flux.error(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied"));
            }
            
            java.time.LocalDate filterDate = null;
            if (filter.date() != null) {
                filterDate = java.time.LocalDate.parse(filter.date());
            }
            DailyProgress probe = new DailyProgress(filter.id(), filter.userId(), filterDate, filter.targetQuestions(), filter.answeredQuestions(), filter.correctQuestions(), filter.wrongQuestions(), null, null, null);
            ExampleMatcher matcher = ExampleMatcher.matching()
                    .withIgnoreNullValues();
            return repository.findAll(Example.of(probe, matcher));
        });
    }
}
