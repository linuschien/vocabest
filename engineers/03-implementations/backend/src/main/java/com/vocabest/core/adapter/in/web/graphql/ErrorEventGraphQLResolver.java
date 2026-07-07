package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.ErrorEventFilterInput;
import com.vocabest.core.adapter.out.persistence.model.ErrorEvent;
import com.vocabest.core.adapter.out.persistence.repository.ErrorEventRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import reactor.core.publisher.Flux;

@Controller
public class ErrorEventGraphQLResolver {

    private final ErrorEventRepository repository;

    public ErrorEventGraphQLResolver(ErrorEventRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    public Flux<ErrorEvent> listErrorEvents(@Argument ErrorEventFilterInput filter) {
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
            
            ErrorEvent probe = new ErrorEvent(filter.id(), filter.userId(), filter.quizQuestionId(), filter.timestamp(), filter.selectedDistractor(), null, null, null);
            ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
            return repository.findAll(Example.of(probe, matcher));
        });
    }
}
