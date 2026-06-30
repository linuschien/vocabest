package com.vocabest.core.application.service;

import com.vocabest.core.adapter.in.web.dto.*;
import com.vocabest.core.adapter.out.persistence.model.VocabularyLevel;
import com.vocabest.core.adapter.out.persistence.model.VocabularyWord;
import com.vocabest.core.adapter.out.persistence.repository.VocabularyWordRepository;
import com.vocabest.core.application.port.in.VocabularyWordCommandService;
import com.vocabest.core.application.port.in.VocabularyWordQueryService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class VocabularyWordServiceImpl implements VocabularyWordCommandService, VocabularyWordQueryService {

    private final VocabularyWordRepository repository;

    public VocabularyWordServiceImpl(VocabularyWordRepository repository) {
        this.repository = repository;
    }

    @Override
    public Mono<VocabularyWordResponse> createVocabularyWord(VocabularyWordRequest req) {
        VocabularyWord entity = new VocabularyWord(null, req.word(), req.partOfSpeech(), req.translation(),
                VocabularyLevel.valueOf(req.level()), req.examFrequency(), null, null, null);
        return repository.save(entity).map(this::mapToResponse);
    }

    @Override
    public Mono<VocabularyWordResponse> updateVocabularyWord(UUID id, VocabularyWordRequest req) {
        return repository.findById(id)
                .map(existing -> new VocabularyWord(existing.id(), req.word(), req.partOfSpeech(), req.translation(),
                        VocabularyLevel.valueOf(req.level()), req.examFrequency(), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(repository::save)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<Void> deleteVocabularyWord(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<OperationStatus> importBulk(VocabularyWordActionRequest req) {
        return Mono.just(new OperationStatus(true, "Bulk imported"));
    }

    @Override
    public Mono<VocabularyWordResponse> getVocabularyWordById(UUID id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public Flux<VocabularyWordResponse> listVocabularyWords(VocabularyWordFilterInput filter) {
        return repository.findAll().map(this::mapToResponse);
    }

    private VocabularyWordResponse mapToResponse(VocabularyWord entity) {
        return new VocabularyWordResponse(
                entity.id(),
                entity.word(),
                entity.partOfSpeech(),
                entity.translation(),
                entity.level().name(),
                entity.examFrequency()
        );
    }
}
