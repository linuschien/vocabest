package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.out.persistence.model.Role;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.User;
import com.vocabest.core.adapter.out.persistence.model.WordBank;
import com.vocabest.core.adapter.out.persistence.model.WordMastery;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class WordMasteryRepositoryIntegrationTest {

    @Autowired
    private WordMasteryRepository wordMasteryRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WordBankRepository wordBankRepository;

    private UUID testUserId;
    private UUID testWordBankId1;
    private UUID testWordBankId2;
    private WordMastery mastery1;
    private WordMastery mastery2;

    @BeforeEach
    void setUp() {
        User user = new User(
                null,
                "word_mastery_repo@example.com",
                Role.LEARNER,
                TargetLevel.JUNIOR_HIGH,
                0,
                20,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
        testUserId = userRepository.save(user).block().id();

        WordBank wordBank1 = new WordBank(
                null, "test1", "n.", "測試1", TargetLevel.JUNIOR_HIGH, 1, 1, LocalDateTime.now(), LocalDateTime.now(), null
        );
        testWordBankId1 = wordBankRepository.save(wordBank1).block().id();

        WordBank wordBank2 = new WordBank(
                null, "test2", "n.", "測試2", TargetLevel.JUNIOR_HIGH, 1, 1, LocalDateTime.now(), LocalDateTime.now(), null
        );
        testWordBankId2 = wordBankRepository.save(wordBank2).block().id();

        // Mastery 1: Ready to review, error weight 5
        WordMastery m1 = new WordMastery(
                null, testUserId, testWordBankId1, 5, LocalDateTime.now().minusDays(1), LocalDateTime.now(), LocalDateTime.now(), null
        );
        mastery1 = wordMasteryRepository.save(m1).block();

        // Mastery 2: Ready to review, error weight 10 (Higher error weight should be prioritized)
        WordMastery m2 = new WordMastery(
                null, testUserId, testWordBankId2, 10, LocalDateTime.now().minusDays(2), LocalDateTime.now(), LocalDateTime.now(), null
        );
        mastery2 = wordMasteryRepository.save(m2).block();
    }

    @AfterEach
    void cleanUp() {
        wordMasteryRepository.deleteById(mastery1.id()).block();
        wordMasteryRepository.deleteById(mastery2.id()).block();
        wordBankRepository.deleteById(testWordBankId1).block();
        wordBankRepository.deleteById(testWordBankId2).block();
        userRepository.deleteById(testUserId).block();
    }

    @Test
    void shouldFindNextReviewWordWithHighestErrorWeight() {
        LocalDateTime now = LocalDateTime.now();

        StepVerifier.create(wordMasteryRepository.findFirstByUserIdAndNextReviewDateLessThanEqualAndErrorWeightGreaterThanOrderByErrorWeightDescNextReviewDateAsc(testUserId, now, 0))
                .assertNext(found -> {
                    // It should return mastery2 because error weight 10 > 5
                    assertThat(found.id()).isEqualTo(mastery2.id());
                    assertThat(found.wordBankId()).isEqualTo(testWordBankId2);
                })
                .verifyComplete();
    }

    @Test
    void shouldCountWordsToReview() {
        LocalDateTime now = LocalDateTime.now();

        StepVerifier.create(wordMasteryRepository.countByUserIdAndNextReviewDateLessThanEqualAndErrorWeightGreaterThan(testUserId, now, 0))
                .expectNext(2L)
                .verifyComplete();
    }
}
