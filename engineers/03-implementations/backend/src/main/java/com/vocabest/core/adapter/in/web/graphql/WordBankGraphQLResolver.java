package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.WordBankFilterInput;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import com.vocabest.core.adapter.out.persistence.repository.WordBankRepository;
import com.vocabest.core.adapter.in.web.dto.WordBankPage;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import com.vocabest.core.adapter.out.persistence.model.Role;
import com.vocabest.core.adapter.out.persistence.model.User;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Mono;

@Controller
public class WordBankGraphQLResolver {

    private final WordBankRepository repository;

    public WordBankGraphQLResolver(WordBankRepository repository) {
        this.repository = repository;
    }

    @QueryMapping
    public Mono<WordBankPage> listWordBanks(@Argument WordBankFilterInput filter) {
        return Mono.deferContextual(ctx -> {
            User user = ctx.getOrDefault("CURRENT_USER", null);
            if (user != null && user.role() != Role.ADMIN && user.targetLevel() != null) {
                WordBankFilterInput overriddenFilter = new WordBankFilterInput(
                    filter != null ? filter.word() : null,
                    filter != null ? filter.startingLetter() : null,
                    filter != null ? filter.difficultyLevel() : null,
                    user.targetLevel().name(),
                    filter != null ? filter.page() : null,
                    filter != null ? filter.size() : null
                );
                return repository.search(overriddenFilter);
            }
            return repository.search(filter);
        });
    }
}
