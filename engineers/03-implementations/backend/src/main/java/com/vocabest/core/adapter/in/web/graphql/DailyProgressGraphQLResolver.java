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
        return Flux.deferContextual(ctx -> {
            com.vocabest.core.adapter.out.persistence.model.User currentUser = ctx.getOrDefault("CURRENT_USER", null);
            if (currentUser == null) {
                return Flux.error(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.UNAUTHORIZED, "Unauthenticated"));
            }
            if (currentUser.role() != com.vocabest.core.adapter.out.persistence.model.Role.ADMIN) {
                if (filter != null && filter.userId() != null && !currentUser.id().equals(filter.userId())) {
                    return Flux.error(new org.springframework.web.server.ResponseStatusException(org.springframework.http.HttpStatus.FORBIDDEN, "Access denied"));
                }
            }
            
            java.util.UUID effectiveUserId = (filter != null && filter.userId() != null) ? filter.userId() : (currentUser.role() == com.vocabest.core.adapter.out.persistence.model.Role.ADMIN ? null : currentUser.id());
            
            java.time.LocalDate filterDate = null;
            if (filter != null && filter.date() != null) {
                filterDate = java.time.LocalDate.parse(filter.date());
            }
            DailyProgress probe = new DailyProgress(
                filter != null ? filter.id() : null, 
                effectiveUserId, 
                filterDate, 
                filter != null ? filter.targetQuestions() : null, 
                filter != null ? filter.answeredQuestions() : null, 
                filter != null ? filter.correctQuestions() : null, 
                filter != null ? filter.wrongQuestions() : null, 
                null, null, null);
            ExampleMatcher matcher = ExampleMatcher.matching()
                    .withIgnoreNullValues();
            return repository.findAll(Example.of(probe, matcher));
        });
    }
}
