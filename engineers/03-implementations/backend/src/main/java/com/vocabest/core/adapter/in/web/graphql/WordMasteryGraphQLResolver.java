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
        if (filter != null && filter.userId() != null) {
            WordMastery probe = new WordMastery(null, filter.userId(), null, null, null, null, null, null);
            ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
            return repository.findAll(Example.of(probe, matcher));
        }
        return repository.findAll();
    }
}
