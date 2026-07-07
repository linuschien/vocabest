package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.UserFilterInput;
import com.vocabest.core.adapter.out.persistence.model.User;
import com.vocabest.core.adapter.out.persistence.repository.UserRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import reactor.core.publisher.Flux;

@Controller
public class UserGraphQLResolver {

    private final UserRepository repository;

    public UserGraphQLResolver(UserRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    @com.vocabest.core.adapter.in.web.security.AdminOnly
    public Flux<User> listUsers(@Argument UserFilterInput filter) {
        if (filter != null && filter.targetLevel() != null) {
            User probe = new User(null, null, null, com.vocabest.core.adapter.out.persistence.model.TargetLevel.valueOf(filter.targetLevel()), null, null, null, null, null);
            ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
            return repository.findAll(Example.of(probe, matcher));
        }
        return repository.findAll();
    }
}
