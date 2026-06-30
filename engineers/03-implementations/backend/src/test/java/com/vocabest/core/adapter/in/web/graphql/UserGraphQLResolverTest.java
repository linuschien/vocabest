package com.vocabest.core.adapter.in.web.graphql;

import com.vocabest.core.adapter.in.web.dto.UserFilterInput;
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

    @InjectMocks
    private UserGraphQLResolver resolver;

    @Test
    void testListUsers() {
        User user = new User(UUID.randomUUID(), TargetLevel.JUNIOR_HIGH, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(repository.findAll()).thenReturn(Flux.just(user));

        StepVerifier.create(resolver.listUsers(new UserFilterInput("JUNIOR_HIGH")))
                .expectNextMatches(e -> e.targetLevel() == TargetLevel.JUNIOR_HIGH)
                .verifyComplete();
    }
}
