package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.UserFilterInput;
import com.vocabest.core.adapter.out.persistence.model.User;
import com.vocabest.core.adapter.out.persistence.repository.UserRepository;
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
        // Return domain models here since the GraphQL schema matches it.
        return repository.findAll();
    }
}
