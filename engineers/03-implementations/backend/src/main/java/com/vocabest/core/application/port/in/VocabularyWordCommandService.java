package com.vocabest.core.application.port.in;

import com.vocabest.core.adapter.in.web.dto.VocabularyWordRequest;
import com.vocabest.core.adapter.in.web.dto.VocabularyWordResponse;
import com.vocabest.core.adapter.in.web.dto.VocabularyWordActionRequest;
import com.vocabest.core.adapter.in.web.dto.OperationStatus;
import reactor.core.publisher.Mono;
import java.util.UUID;

public interface VocabularyWordCommandService {
    Mono<VocabularyWordResponse> createVocabularyWord(VocabularyWordRequest req);
    Mono<VocabularyWordResponse> updateVocabularyWord(UUID id, VocabularyWordRequest req);
    Mono<Void> deleteVocabularyWord(UUID id);
    Mono<OperationStatus> importBulk(VocabularyWordActionRequest req);
}
