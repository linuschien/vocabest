package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.QuizQuestionResponse;
import com.vocabest.core.adapter.in.web.dto.SubmitAnswerRequest;
import com.vocabest.core.adapter.in.web.dto.SubmitAnswerResponse;
import com.vocabest.core.adapter.in.web.dto.UserOnboardRequest;
import com.vocabest.core.adapter.in.web.dto.UserPatchRequest;
import com.vocabest.core.adapter.in.web.dto.UserRequest;
import com.vocabest.core.adapter.out.persistence.model.Role;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.User;
import com.vocabest.core.application.port.in.UserCommandService;
import com.vocabest.core.application.port.in.UserQueryService;
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
    private UserCommandService commandService;
    
    @Mock
    private UserQueryService queryService;

    @InjectMocks
    private UserRestController controller;

    private WebTestClient client;

    @BeforeEach
    void setUp() {
        client = WebTestClient.bindToController(controller)
                .webFilter((exchange, chain) -> {
                    String emailHeader = exchange.getRequest().getHeaders().getFirst("x-goog-authenticated-user-email");
                    String email = emailHeader != null ? emailHeader.replace("accounts.google.com:", "") : "test@test.com";
                    User dummyUser = new User(UUID.randomUUID(), email, Role.ADMIN, TargetLevel.JUNIOR_HIGH, 0, 0, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
                    return chain.filter(exchange).contextWrite(reactor.util.context.Context.of("CURRENT_USER", dummyUser, "CURRENT_EMAIL", email));
                })
                .build();
    }

    @Test
    void testCreateUser() {
        User user = new User(UUID.randomUUID(), "test@test.com", Role.LEARNER, TargetLevel.JUNIOR_HIGH, 0, 0, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        when(commandService.createUser(any())).thenReturn(Mono.just(user));

        client.post().uri("/api/v1/users")
                .bodyValue(new UserRequest("test@test.com", "LEARNER", "JUNIOR_HIGH", 0, 20))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.id").isNotEmpty();
    }

    @Test
    void testGetUserById() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "test@test.com", Role.LEARNER, TargetLevel.JUNIOR_HIGH, 0, 0, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        when(queryService.getUserById(id)).thenReturn(Mono.just(user));

        client.get().uri("/api/v1/users/{id}", id)
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.id").isEqualTo(id.toString());
    }

    @Test
    void testGetUserById_notFound() {
        UUID id = UUID.randomUUID();
        when(queryService.getUserById(id)).thenReturn(Mono.empty());

        client.get().uri("/api/v1/users/{id}", id)
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testUpdateUser() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "test@test.com", Role.LEARNER, TargetLevel.JUNIOR_HIGH, 0, 0, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        when(commandService.updateUser(any(), any())).thenReturn(Mono.just(user));

        client.put().uri("/api/v1/users/{id}", id)
                .bodyValue(new UserRequest("test@test.com", "LEARNER", "JUNIOR_HIGH", 0, 20))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testUpdateUser_notFound() {
        UUID id = UUID.randomUUID();
        when(commandService.updateUser(any(), any())).thenReturn(Mono.empty());

        client.put().uri("/api/v1/users/{id}", id)
                .bodyValue(new UserRequest("test@test.com", "LEARNER", "JUNIOR_HIGH", 0, 20))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testPatchUser() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "test@test.com", Role.LEARNER, TargetLevel.SENIOR_HIGH, 0, 0, 0, 30, LocalDateTime.now(), LocalDateTime.now(), null);
        when(commandService.patchUser(any(), any())).thenReturn(Mono.just(user));

        client.patch().uri("/api/v1/users/{id}", id)
                .bodyValue(new UserPatchRequest("SENIOR_HIGH", 30))
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testPatchUser_notFound() {
        UUID id = UUID.randomUUID();
        when(commandService.patchUser(any(), any())).thenReturn(Mono.empty());

        client.patch().uri("/api/v1/users/{id}", id)
                .bodyValue(new UserPatchRequest("SENIOR_HIGH", 30))
                .exchange()
                .expectStatus().isNotFound();
    }

    @Test
    void testDeleteUser() {
        UUID id = UUID.randomUUID();
        when(commandService.deleteUser(id)).thenReturn(Mono.empty());

        client.delete().uri("/api/v1/users/{id}", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testOnboardUser() {
        User user = new User(UUID.randomUUID(), "test@test.com", Role.LEARNER, TargetLevel.JUNIOR_HIGH, 0, 0, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        when(commandService.onboardUser(any())).thenReturn(Mono.just(user));

        client.post().uri("/api/v1/users:onboard")
                .header("x-goog-authenticated-user-email", "accounts.google.com:test@test.com")
                .bodyValue(new UserOnboardRequest("test@test.com", "JUNIOR_HIGH", 20))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.id").isNotEmpty();
    }

    @Test
    void testOnboardUser_withNullRoleAndTargetLevel() {
        User user = new User(UUID.randomUUID(), "test@test.com", null, null, 0, 0, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        when(commandService.onboardUser(any())).thenReturn(Mono.just(user));

        client.post().uri("/api/v1/users:onboard")
                .header("x-goog-authenticated-user-email", "accounts.google.com:test@test.com")
                .bodyValue(new UserOnboardRequest("test@test.com", "JUNIOR_HIGH", 20))
                .exchange()
                .expectStatus().isCreated()
                .expectBody().jsonPath("$.role").isEmpty()
                .jsonPath("$.targetLevel").isEmpty();
    }

    @Test
    void testGetNextQuestion() {
        UUID id = UUID.randomUUID();
        QuizQuestionResponse response = new QuizQuestionResponse(UUID.randomUUID(), UUID.randomUUID().toString(), "cloze", "chinese", "correct", "d1", "d2", "d3", "root", "mnem");
        when(queryService.getNextQuestion(id)).thenReturn(Mono.just(response));

        client.post().uri("/api/v1/users/{id}:nextQuestion", id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testGetNextQuestion_notFound() {
        UUID id = UUID.randomUUID();
        when(queryService.getNextQuestion(id)).thenReturn(Mono.empty());

        client.post().uri("/api/v1/users/{id}:nextQuestion", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testGetNextErrorQuestion() {
        UUID id = UUID.randomUUID();
        QuizQuestionResponse response = new QuizQuestionResponse(UUID.randomUUID(), UUID.randomUUID().toString(), "cloze", "chinese", "correct", "d1", "d2", "d3", "root", "mnem");
        when(queryService.getNextErrorQuestion(id)).thenReturn(Mono.just(response));

        client.post().uri("/api/v1/users/{id}:nextErrorQuestion", id)
                .exchange()
                .expectStatus().isOk();
    }

    @Test
    void testGetNextErrorQuestion_notFound() {
        UUID id = UUID.randomUUID();
        when(queryService.getNextErrorQuestion(id)).thenReturn(Mono.empty());

        client.post().uri("/api/v1/users/{id}:nextErrorQuestion", id)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void testSubmitAnswer() {
        UUID id = UUID.randomUUID();
        SubmitAnswerResponse response = new SubmitAnswerResponse(true, "correct", "root", "mnem");
        when(commandService.submitAnswer(any(), any())).thenReturn(Mono.just(response));

        client.post().uri("/api/v1/users/{id}:submitAnswer", id)
                .bodyValue(new SubmitAnswerRequest(UUID.randomUUID(), "correct"))
                .exchange()
                .expectStatus().isOk()
                .expectBody().jsonPath("$.isCorrect").isEqualTo(true);
    }

    @Test
    void testSubmitAnswer_notFound() {
        UUID id = UUID.randomUUID();
        when(commandService.submitAnswer(any(), any())).thenReturn(Mono.empty());

        client.post().uri("/api/v1/users/{id}:submitAnswer", id)
                .bodyValue(new SubmitAnswerRequest(UUID.randomUUID(), "correct"))
                .exchange()
                .expectStatus().isNotFound();
    }
}
