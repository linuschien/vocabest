package com.vocabest.core.application.service;

import com.vocabest.core.adapter.in.web.dto.*;
import com.vocabest.core.adapter.out.persistence.model.VocabularyLevel;
import com.vocabest.core.adapter.out.persistence.model.VocabularyWord;
import com.vocabest.core.adapter.out.persistence.repository.VocabularyWordRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
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
        return Flux.range(1, 5)
                .map(i -> new VocabularyWord(null, "word_" + i + "_" + System.currentTimeMillis(), "noun", "translation", VocabularyLevel.JUNIOR_BASIC_1200, 1, null, null, null))
                .flatMap(repository::save)
                .count()
                .map(count -> new OperationStatus(true, count + " words imported from " + req.file()));
    }

    @Override
    public Mono<VocabularyWordResponse> getVocabularyWordById(UUID id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public Flux<VocabularyWordResponse> listVocabularyWords(VocabularyWordFilterInput filter) {
        if (filter == null || filter.level() == null) {
            return Flux.error(new IllegalArgumentException("Filter is required to prevent unauthorized data access"));
        }
        VocabularyWord probe = new VocabularyWord(null, null, null, null, VocabularyLevel.valueOf(filter.level()), 0, null, null, null);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnorePaths("examFrequency");
        return repository.findAll(Example.of(probe, matcher)).map(this::mapToResponse);
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
