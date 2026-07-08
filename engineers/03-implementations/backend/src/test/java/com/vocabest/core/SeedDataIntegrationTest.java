package com.vocabest.core;

import com.vocabest.core.adapter.out.persistence.model.WordBank;
import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import com.vocabest.core.adapter.out.persistence.repository.WordBankRepository;
import com.vocabest.core.adapter.out.persistence.repository.QuizQuestionRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class SeedDataIntegrationTest {

    @Autowired
    private WordBankRepository wordBankRepository;

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Test
    void shouldLoadSeedDataSuccessfully() {
        // Find the word 'a' which is seeded in V1_1__Seed_Vocabulary_Junior_High.sql
        // with ID '4c6c0fcc-cb90-4b1a-88cc-010c3254fef6'
        UUID wordId = UUID.fromString("4c6c0fcc-cb90-4b1a-88cc-010c3254fef6");
        
        StepVerifier.create(wordBankRepository.findById(wordId))
                .expectNextMatches(wordBank -> {
                    return wordBank.word().equals("a") &&
                           wordBank.chineseTranslation().equals("一個;一種") &&
                           wordBank.targetLevel().name().equals("JUNIOR_HIGH");
                })
                .verifyComplete();

        // Also check that there are quiz questions populated
        StepVerifier.create(quizQuestionRepository.findAll().take(10).collectList())
                .expectNextMatches(list -> list.size() > 0)
                .verifyComplete();
    }
}
