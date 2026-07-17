package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.UserFilterInput;
import com.vocabest.core.adapter.out.persistence.model.Role;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.User;
import com.vocabest.core.adapter.out.persistence.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserGraphQLResolverTest {

    @Mock
    private UserRepository repository;

    @Mock
    private com.vocabest.core.adapter.out.persistence.repository.DailyProgressRepository dailyProgressRepository;

    @InjectMocks
    private UserGraphQLResolver resolver;

    @Test
    void testListUsers() {
        when(repository.findAll()).thenReturn(Flux.empty());
        StepVerifier.create(resolver.listUsers(null))
                .verifyComplete();
    }

    @Test
    void testListUsersWithFilter() {
        User user = new User(UUID.randomUUID(), "test@test.com", Role.LEARNER, TargetLevel.JUNIOR_HIGH, 0, 0, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.findAll(org.mockito.ArgumentMatchers.<org.springframework.data.domain.Example<User>>any())).thenReturn(Flux.just(user));

        StepVerifier.create(resolver.listUsers(new UserFilterInput(null, null, null, "JUNIOR_HIGH", null, null)))
                .expectNextMatches(e -> e.targetLevel() == TargetLevel.JUNIOR_HIGH)
                .verifyComplete();
    }

    @Test
    void testStatsBatchMapping() {
        User user1 = new User(UUID.randomUUID(), "test1@test.com", Role.LEARNER, TargetLevel.JUNIOR_HIGH, 0, 0, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        User user2 = new User(UUID.randomUUID(), "test2@test.com", Role.LEARNER, TargetLevel.JUNIOR_HIGH, 0, 0, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        
        com.vocabest.core.adapter.out.persistence.repository.UserAggregatedStats stats1 = new com.vocabest.core.adapter.out.persistence.repository.UserAggregatedStats(user1.id(), 100, 80);
        
        when(dailyProgressRepository.findAggregatedStatsByUserIds(any())).thenReturn(Flux.just(stats1));
        
        StepVerifier.create(resolver.stats(java.util.List.of(user1, user2)))
                .expectNextMatches(map -> {
                    UserGraphQLResolver.UserStats u1Stats = map.get(user1);
                    UserGraphQLResolver.UserStats u2Stats = map.get(user2);
                    return u1Stats.totalQuestionsAnswered() == 100 
                        && u1Stats.totalCorrectAnswers() == 80 
                        && u1Stats.overallAccuracy().equals("80.0%")
                        && u2Stats.totalQuestionsAnswered() == 0
                        && u2Stats.totalCorrectAnswers() == 0
                        && u2Stats.overallAccuracy().equals("0.0%");
                })
                .verifyComplete();
    }
}
