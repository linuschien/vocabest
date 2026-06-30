package com.vocabest.core.application.port.in;

import com.vocabest.core.adapter.in.web.dto.VocabularyWordResponse;
import com.vocabest.core.adapter.in.web.dto.VocabularyWordFilterInput;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface VocabularyWordQueryService {
    Mono<VocabularyWordResponse> getVocabularyWordById(UUID id);
    Flux<VocabularyWordResponse> listVocabularyWords(VocabularyWordFilterInput filter);
}
