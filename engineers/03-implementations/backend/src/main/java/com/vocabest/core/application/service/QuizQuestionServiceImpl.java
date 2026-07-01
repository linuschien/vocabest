package com.vocabest.core.application.service;

import com.vocabest.core.adapter.in.web.dto.*;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.repository.QuizQuestionRepository;
import com.vocabest.core.adapter.out.persistence.repository.VocabularyWordRepository;
import com.vocabest.core.adapter.out.persistence.model.VocabularyLevel;
import com.vocabest.core.adapter.out.persistence.model.VocabularyWord;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import com.vocabest.core.application.port.in.QuizQuestionCommandService;
import com.vocabest.core.application.port.in.QuizQuestionQueryService;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class QuizQuestionServiceImpl implements QuizQuestionCommandService, QuizQuestionQueryService {

    private final QuizQuestionRepository repository;
    private final VocabularyWordRepository vocabRepository;

    public QuizQuestionServiceImpl(QuizQuestionRepository repository, VocabularyWordRepository vocabRepository) {
        this.repository = repository;
        this.vocabRepository = vocabRepository;
    }

    @Override
    public Mono<QuizQuestionResponse> createQuizQuestion(QuizQuestionRequest req) {
        QuizQuestion entity = new QuizQuestion(null, UUID.fromString(req.wordId()), req.contextualCloze(),
                req.translation(), req.correctOption(), req.distractor1(), req.distractor2(), req.distractor3(),
                req.explanationRootAffix(), req.explanationMnemonic(), TargetLevel.valueOf(req.targetLevel()),
                null, null, null);
        return repository.save(entity).map(this::mapToResponse);
    }

    @Override
    public Mono<QuizQuestionResponse> updateQuizQuestion(UUID id, QuizQuestionRequest req) {
        return repository.findById(id)
                .map(existing -> new QuizQuestion(existing.id(), UUID.fromString(req.wordId()), req.contextualCloze(),
                        req.translation(), req.correctOption(), req.distractor1(), req.distractor2(), req.distractor3(),
                        req.explanationRootAffix(), req.explanationMnemonic(), TargetLevel.valueOf(req.targetLevel()),
                        existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(repository::save)
                .map(this::mapToResponse);
    }

    @Override
    public Mono<Void> deleteQuizQuestion(UUID id) {
        return repository.deleteById(id);
    }

    @Override
    public Mono<OperationStatus> generateBatch(QuizQuestionActionRequest req) {
        VocabularyLevel vLevel = req.targetLevel().equals("JUNIOR_HIGH") ? 
            VocabularyLevel.JUNIOR_BASIC_1200 : 
            VocabularyLevel.SENIOR_LEVEL_1;
        
        VocabularyWord probe = new VocabularyWord(null, null, null, null, vLevel, 0, null, null, null);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues()
                .withIgnorePaths("examFrequency");
        
        return vocabRepository.findAll(Example.of(probe, matcher))
                .take(req.maxWordsToProcess())
                .map(word -> new QuizQuestion(null, word.id(), "Contextual cloze for " + word.word(), "Translation for " + word.word(), word.word(), "Distractor 1", "Distractor 2", "Distractor 3", "Root details", "Mnemonic details", TargetLevel.valueOf(req.targetLevel()), null, null, null))
                .flatMap(repository::save)
                .count()
                .map(count -> new OperationStatus(true, "Batch generated for " + count + " questions"));
    }

    @Override
    public Mono<QuizQuestionResponse> getQuizQuestionById(UUID id) {
        return repository.findById(id).map(this::mapToResponse);
    }

    @Override
    public Flux<QuizQuestionResponse> listQuizQuestions(QuizQuestionFilterInput filter) {
        if (filter == null || filter.targetLevel() == null) {
            return Flux.error(new IllegalArgumentException("Filter is required to prevent unauthorized data access"));
        }
        QuizQuestion probe = new QuizQuestion(null, null, null, null, null, null, null, null, null, null, TargetLevel.valueOf(filter.targetLevel()), null, null, null);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnoreNullValues();
        return repository.findAll(Example.of(probe, matcher)).map(this::mapToResponse);
    }

    private QuizQuestionResponse mapToResponse(QuizQuestion entity) {
        return new QuizQuestionResponse(
                entity.id(),
                entity.vocabularyWordId().toString(),
                entity.contextualCloze(),
                entity.translation(),
                entity.correctOption(),
                entity.distractor1(),
                entity.distractor2(),
                entity.distractor3(),
                entity.explanationRootAffix(),
                entity.explanationMnemonic(),
                entity.targetLevel().name()
        );
    }
}
