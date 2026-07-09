package com.vocabest.core.adapter.out.persistence.repository;

import com.vocabest.core.adapter.out.persistence.model.Role;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class UserRepositoryIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @AfterEach
    void cleanUp() {
        userRepository.findByEmail("repo_test@example.com")
                .flatMap(user -> userRepository.delete(user))
                .block();
    }

    @Test
    void shouldSaveAndFindByEmail() {
        User user = new User(
                null,
                "repo_test@example.com",
                Role.LEARNER,
                TargetLevel.JUNIOR_HIGH,
                0,
                20,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null
        );

        StepVerifier.create(userRepository.save(user)
                        .flatMap(savedUser -> userRepository.findByEmail("repo_test@example.com")))
                .assertNext(foundUser -> {
                    assertThat(foundUser.id()).isNotNull();
                    assertThat(foundUser.email()).isEqualTo("repo_test@example.com");
                    assertThat(foundUser.role()).isEqualTo(Role.LEARNER);
                    assertThat(foundUser.targetLevel()).isEqualTo(TargetLevel.JUNIOR_HIGH);
                })
                .verifyComplete();
    }
}
