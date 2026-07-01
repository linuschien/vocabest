package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.UserFilterInput;
import com.vocabest.core.adapter.out.persistence.model.User;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.repository.UserRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class UserGraphQLResolver {

    private final UserRepository repository;

    public UserGraphQLResolver(UserRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    public Flux<User> listUsers(@Argument UserFilterInput filter) {
        if (filter == null || filter.targetLevel() == null) {
            return Flux.error(new IllegalArgumentException("Filter is required to prevent unauthorized data access"));
        }
        User probe = new User(null, TargetLevel.valueOf(filter.targetLevel()), 0, 0, null, null, null);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnorePaths("learningStreak", "dailyTargetQuestions");
        return repository.findAll(Example.of(probe, matcher));
    }
}
