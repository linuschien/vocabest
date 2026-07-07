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
        if (filter != null && filter.userId() != null) {
            ErrorEvent probe = new ErrorEvent(null, filter.userId(), null, null, null, null, null, null);
            ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
            return repository.findAll(Example.of(probe, matcher));
        }
        return repository.findAll();
    }
}
