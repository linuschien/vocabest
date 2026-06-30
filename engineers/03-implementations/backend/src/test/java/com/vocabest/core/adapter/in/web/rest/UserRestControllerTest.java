package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.UserRequest;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.User;
import com.vocabest.core.adapter.out.persistence.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRestControllerTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private UserRestController controller;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToController(controller).build();
    }

    @Test
    void testCreateUser() {
        User user = new User(UUID.randomUUID(), TargetLevel.JUNIOR_HIGH, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.save(any())).thenReturn(Mono.just(user));

        client.post().uri("/api/v1/users")
                .bodyValue(new UserRequest("JUNIOR_HIGH", 0, 20))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.id").isNotEmpty();
    }

    @Test
    void testGetUserById() {
        UUID id = UUID.randomUUID();
        User user = new User(id, TargetLevel.JUNIOR_HIGH, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findById(id)).thenReturn(Mono.just(user));

        client.get().uri("/api/v1/users/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void testUpdateUser() {
        UUID id = UUID.randomUUID();
        User user = new User(id, TargetLevel.JUNIOR_HIGH, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        when(repository.findById(id)).thenReturn(Mono.just(user));
        when(repository.save(any())).thenReturn(Mono.just(user));

        client.put().uri("/api/v1/users/{id}", id)
                .bodyValue(new UserRequest("JUNIOR_HIGH", 0, 20))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testDeleteUser() {
        UUID id = UUID.randomUUID();
        when(repository.deleteById(id)).thenReturn(Mono.empty());

        client.delete().uri("/api/v1/users/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }
}
