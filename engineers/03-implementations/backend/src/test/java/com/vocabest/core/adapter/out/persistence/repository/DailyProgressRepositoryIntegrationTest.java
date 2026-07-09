package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.out.persistence.model.DailyProgress;
import com.vocabest.core.adapter.out.persistence.model.Role;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DailyProgressRepositoryIntegrationTest {

    @Autowired
    private DailyProgressRepository dailyProgressRepository;

    @Autowired
    private UserRepository userRepository;

    private UUID testUserId;

    @BeforeEach
    void setUp() {
        User user = new User(
                null,
                "daily_progress_repo@example.com",
                Role.LEARNER,
                TargetLevel.JUNIOR_HIGH,
                0,
                20,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );
        testUserId = userRepository.save(user).block().id();
    }

    @AfterEach
    void cleanUp() {
        dailyProgressRepository.findByUserIdAndDate(testUserId, LocalDate.now())
                .flatMap(dp -> dailyProgressRepository.delete(dp))
                .block();
        userRepository.deleteById(testUserId).block();
    }

    @Test
    void shouldSaveAndFindByUserIdAndDate() {
        LocalDate today = LocalDate.now();
        DailyProgress progress = new DailyProgress(
                null,
                testUserId,
                today,
                20,
                15,
                10,
                5,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        StepVerifier.create(dailyProgressRepository.save(progress)
                        .flatMap(saved -> dailyProgressRepository.findByUserIdAndDate(testUserId, today)))
                .assertNext(found -> {
                    assertThat(found.id()).isNotNull();
                    assertThat(found.userId()).isEqualTo(testUserId);
                    assertThat(found.date()).isEqualTo(today);
                    assertThat(found.targetQuestions()).isEqualTo(20);
                })
                .verifyComplete();
    }
}
