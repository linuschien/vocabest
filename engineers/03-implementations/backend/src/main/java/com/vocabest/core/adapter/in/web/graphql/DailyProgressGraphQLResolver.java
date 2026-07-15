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
        if (filter != null && filter.startDate() != null && filter.endDate() != null) {
            java.time.LocalDate start = java.time.LocalDate.parse(filter.startDate());
            java.time.LocalDate end = java.time.LocalDate.parse(filter.endDate());
            
            Flux<DailyProgress> flux;
            if (filter.userId() != null) {
                flux = repository.findByUserIdAndDateBetween(filter.userId(), start, end);
            } else {
                flux = repository.findByDateBetween(start, end);
            }
            
            return flux.filter(dp -> {
                if (filter.id() != null && !filter.id().equals(dp.id())) return false;
                if (filter.targetQuestions() != null && !filter.targetQuestions().equals(dp.targetQuestions())) return false;
                if (filter.answeredQuestions() != null && !filter.answeredQuestions().equals(dp.answeredQuestions())) return false;
                if (filter.correctQuestions() != null && !filter.correctQuestions().equals(dp.correctQuestions())) return false;
                if (filter.wrongQuestions() != null && !filter.wrongQuestions().equals(dp.wrongQuestions())) return false;
                return true;
            });
        }

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
