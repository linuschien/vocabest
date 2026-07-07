package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.WordMasteryFilterInput;
import com.vocabest.core.adapter.out.persistence.model.WordMastery;
import com.vocabest.core.adapter.out.persistence.repository.WordMasteryRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import reactor.core.publisher.Flux;

@Controller
public class WordMasteryGraphQLResolver {

    private final WordMasteryRepository repository;

    public WordMasteryGraphQLResolver(WordMasteryRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    public Flux<WordMastery> listWordMasteries(@Argument WordMasteryFilterInput filter) {
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
            
            WordMastery probe = new WordMastery(
                filter != null ? filter.id() : null, 
                effectiveUserId, 
                filter != null ? filter.wordBankId() : null, 
                filter != null ? filter.errorWeight() : null, 
                null, null, null, null);
            ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
            return repository.findAll(Example.of(probe, matcher));
        });
    }
}
