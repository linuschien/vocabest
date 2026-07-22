package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.WordMasteryFilterInput;
import com.vocabest.core.adapter.out.persistence.model.WordMastery;
import com.vocabest.core.adapter.out.persistence.repository.WordMasteryRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
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
    @com.vocabest.core.adapter.in.web.security.RequireOwnership("#filter?.userId()")
    public Flux<WordMastery> listWordMasteries(@Argument WordMasteryFilterInput filter) {
        WordMastery probe = new WordMastery(
            filter != null ? filter.id() : null, 
            filter != null ? filter.userId() : null, 
            filter != null ? filter.wordBankId() : null, 
            filter != null ? filter.errorWeight() : null, 
            null, null, null, null);
        ExampleMatcher matcher = ExampleMatcher.matching().withIgnoreNullValues();
        return repository.findAll(Example.of(probe, matcher));
    }

    @SchemaMapping(typeName = "WordMastery", field = "nextReviewDate")
    public java.time.OffsetDateTime nextReviewDate(WordMastery mastery) {
        if (mastery.nextReviewDate() == null) return null;
        return mastery.nextReviewDate().atOffset(java.time.ZoneOffset.UTC);
    }
}
