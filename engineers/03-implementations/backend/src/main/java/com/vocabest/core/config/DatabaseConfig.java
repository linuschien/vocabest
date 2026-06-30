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
                    UUID.randomUUID(), entity.targetLevel(), entity.learningStreak(), entity.dailyTargetQuestions(),
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
                    UUID.randomUUID(), entity.userId(), entity.date(), entity.completedQuestions(),
                    LocalDateTime.now(), LocalDateTime.now(), null
                ));
            }
            return reactor.core.publisher.Mono.just(entity);
        };
    }

    @Bean
    public BeforeConvertCallback<VocabularyWord> vocabularyWordBeforeConvertCallback() {
        return (entity, sqlIdentifier) -> {
            if (entity.id() == null) {
                return reactor.core.publisher.Mono.just(new VocabularyWord(
                    UUID.randomUUID(), entity.word(), entity.partOfSpeech(), entity.translation(), entity.level(), entity.examFrequency(),
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
                    UUID.randomUUID(), entity.vocabularyWordId(), entity.contextualCloze(), entity.translation(), entity.correctOption(),
                    entity.distractor1(), entity.distractor2(), entity.distractor3(), entity.explanationRootAffix(), entity.explanationMnemonic(),
                    entity.targetLevel(), LocalDateTime.now(), LocalDateTime.now(), null
                ));
            }
            return reactor.core.publisher.Mono.just(entity);
        };
    }

    @Bean
    public BeforeConvertCallback<ErrorLog> errorLogBeforeConvertCallback() {
        return (entity, sqlIdentifier) -> {
            if (entity.id() == null) {
                return reactor.core.publisher.Mono.just(new ErrorLog(
                    UUID.randomUUID(), entity.userId(), entity.vocabularyWordId(), entity.quizQuestionId(), entity.errorWeight(), entity.nextReviewDate(),
                    LocalDateTime.now(), LocalDateTime.now(), null
                ));
            }
            return reactor.core.publisher.Mono.just(entity);
        };
    }
}
