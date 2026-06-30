package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.VocabularyWordFilterInput;
import com.vocabest.core.adapter.in.web.dto.VocabularyWordResponse;
import com.vocabest.core.application.port.in.VocabularyWordQueryService;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

@Controller
public class VocabularyWordGraphQLResolver {

    private final VocabularyWordQueryService queryService;

    public VocabularyWordGraphQLResolver(VocabularyWordQueryService queryService) {
        this.queryService = queryService;
    }

    @QueryMapping
    public Flux<VocabularyWordResponse> listVocabularyWords(@Argument VocabularyWordFilterInput filter) {
        return queryService.listVocabularyWords(filter);
    }
}
