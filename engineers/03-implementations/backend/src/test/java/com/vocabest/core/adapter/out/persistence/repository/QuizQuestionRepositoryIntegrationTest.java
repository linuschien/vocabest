package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.out.persistence.model.QuizQuestion;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class QuizQuestionRepositoryIntegrationTest {

    @Autowired
    private QuizQuestionRepository quizQuestionRepository;

    @Autowired
    private WordBankRepository wordBankRepository;

    private UUID testWordBankId;
    private QuizQuestion question1;
    private QuizQuestion question2;

    @BeforeEach
    void setUp() {
        WordBank wordBank = new WordBank(
                null, "quiz_repo_test", "n.", "測試", TargetLevel.JUNIOR_HIGH, 1, 1, LocalDateTime.now(), LocalDateTime.now(), null
        );
        testWordBankId = wordBankRepository.save(wordBank).block().id();

        QuizQuestion q1 = new QuizQuestion(
                null, testWordBankId, "I have a ___.", "我有一個蘋果。", "apple", "banana", "orange", "grape", "N/A", "N/A", LocalDateTime.now(), LocalDateTime.now(), null
        );
        question1 = quizQuestionRepository.save(q1).block();

        QuizQuestion q2 = new QuizQuestion(
                null, testWordBankId, "He is a ___.", "他是一個男孩。", "boy", "girl", "man", "woman", "N/A", "N/A", LocalDateTime.now(), LocalDateTime.now(), null
        );
        question2 = quizQuestionRepository.save(q2).block();
    }

    @AfterEach
    void cleanUp() {
        quizQuestionRepository.deleteById(question1.id()).block();
        quizQuestionRepository.deleteById(question2.id()).block();
        wordBankRepository.deleteById(testWordBankId).block();
    }

    @Test
    void shouldFindByWordBankId() {
        StepVerifier.create(quizQuestionRepository.findByWordBankId(testWordBankId).collectList())
                .assertNext(list -> {
                    assertThat(list).hasSize(2);
                    assertThat(list).extracting(QuizQuestion::correctAnswer).containsExactlyInAnyOrder("apple", "boy");
                })
                .verifyComplete();
    }

    @Test
    void shouldFindAllByPageable() {
        // Sort by ID to ensure predictable order (descending to get the latest ones)
        PageRequest pageRequest = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
        
        StepVerifier.create(quizQuestionRepository.findAllBy(pageRequest).collectList())
                .assertNext(list -> {
                    // There might be seed data, so we just check that it returns exactly 2 elements
                    assertThat(list).hasSize(2);
                })
                .verifyComplete();
    }
}
