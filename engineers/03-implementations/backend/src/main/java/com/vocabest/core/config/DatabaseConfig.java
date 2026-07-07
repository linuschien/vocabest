package com.vocabest.core.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.mapping.event.BeforeConvertCallback;
import com.vocabest.core.adapter.out.persistence.model.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Configuration
public class DatabaseConfig {

    @Bean
    public BeforeConvertCallback<User> userBeforeConvertCallback() {
        return (entity, sqlIdentifier) -> {
            if (entity.id() == null) {
                return reactor.core.publisher.Mono.just(new User(
                    UUID.randomUUID(), entity.email(), entity.role(), entity.targetLevel(), entity.learningStreak(), entity.dailyTargetQuestions(),
                    LocalDateTime.now(), LocalDateTime.now(), null
                ));
            }
            return reactor.core.publisher.Mono.just(entity);
        };
    }

    @Bean
    public BeforeConvertCallback<DailyProgress> dailyProgressBeforeConvertCallback() {
        return (entity, sqlIdentifier) -> {
            if (entity.id() == null) {
                return reactor.core.publisher.Mono.just(new DailyProgress(
                    UUID.randomUUID(), entity.userId(), entity.date(), entity.targetQuestions(), entity.answeredQuestions(), entity.correctQuestions(), entity.wrongQuestions(),
                    LocalDateTime.now(), LocalDateTime.now(), null
                ));
            }
            return reactor.core.publisher.Mono.just(entity);
        };
    }

    @Bean
    public BeforeConvertCallback<WordBank> wordBankBeforeConvertCallback() {
        return (entity, sqlIdentifier) -> {
            if (entity.id() == null) {
                return reactor.core.publisher.Mono.just(new WordBank(
                    UUID.randomUUID(), entity.word(), entity.partsOfSpeech(), entity.chineseTranslation(), entity.targetLevel(), entity.difficultyLevel(), entity.examFrequency(),
                    LocalDateTime.now(), LocalDateTime.now(), null
                ));
            }
            return reactor.core.publisher.Mono.just(entity);
        };
    }

    @Bean
    public BeforeConvertCallback<QuizQuestion> quizQuestionBeforeConvertCallback() {
        return (entity, sqlIdentifier) -> {
            if (entity.id() == null) {
                return reactor.core.publisher.Mono.just(new QuizQuestion(
                    UUID.randomUUID(), entity.wordBankId(), entity.contextualCloze(), entity.chineseTranslation(), entity.correctAnswer(),
                    entity.distractor1(), entity.distractor2(), entity.distractor3(), entity.explanationRootAffix(), entity.explanationMnemonic(),
                    LocalDateTime.now(), LocalDateTime.now(), null
                ));
            }
            return reactor.core.publisher.Mono.just(entity);
        };
    }

    @Bean
    public BeforeConvertCallback<ErrorEvent> errorEventBeforeConvertCallback() {
        return (entity, sqlIdentifier) -> {
            if (entity.id() == null) {
                return reactor.core.publisher.Mono.just(new ErrorEvent(
                    UUID.randomUUID(), entity.userId(), entity.quizQuestionId(), entity.timestamp(), entity.selectedDistractor(),
                    LocalDateTime.now(), LocalDateTime.now(), null
                ));
            }
            return reactor.core.publisher.Mono.just(entity);
        };
    }

    @Bean
    public BeforeConvertCallback<WordMastery> wordMasteryBeforeConvertCallback() {
        return (entity, sqlIdentifier) -> {
            if (entity.id() == null) {
                return reactor.core.publisher.Mono.just(new WordMastery(
                    UUID.randomUUID(), entity.userId(), entity.wordBankId(), entity.errorWeight(), entity.nextReviewDate(),
                    LocalDateTime.now(), LocalDateTime.now(), null
                ));
            }
            return reactor.core.publisher.Mono.just(entity);
        };
    }
}
