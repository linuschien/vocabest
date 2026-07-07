package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.WordBankFilterInput;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import com.vocabest.core.adapter.out.persistence.repository.WordBankRepository;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class WordBankGraphQLResolver {

    private final WordBankRepository repository;

    public WordBankGraphQLResolver(WordBankRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    public Flux<WordBank> listWordBanks(@Argument WordBankFilterInput filter) {
        return repository.search(filter);
    }
}
