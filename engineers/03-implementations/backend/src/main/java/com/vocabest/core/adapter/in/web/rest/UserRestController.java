package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.*;
import com.vocabest.core.application.port.in.UserCommandService;
import com.vocabest.core.application.port.in.UserQueryService;
import com.vocabest.core.adapter.out.persistence.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.util.UUID;

@RestController
public class UserRestController {

    private final UserCommandService commandService;
    private final UserQueryService queryService;

    public UserRestController(UserCommandService commandService, UserQueryService queryService) {
        this.commandService = commandService;
        this.queryService = queryService;
    }

    @PostMapping("/api/v1/users")
    @com.vocabest.core.adapter.in.web.security.AdminOnly
    public Mono<ResponseEntity<UserResponse>> createUser(@RequestBody UserRequest req) {
        return commandService.createUser(req)
                .map(this::mapToResponse)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res));
    }

    @GetMapping("/api/v1/users/{id}")
    public Mono<ResponseEntity<UserResponse>> getUserById(@PathVariable UUID id) {
        return verifyOwnership(id).then(queryService.getUserById(id))
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/api/v1/users/{id}")
    public Mono<ResponseEntity<UserResponse>> updateUser(@PathVariable UUID id, @RequestBody UserRequest req) {
        return verifyOwnership(id).then(commandService.updateUser(id, req))
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/api/v1/users/{id}")
    @com.vocabest.core.adapter.in.web.security.AdminOnly
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable UUID id) {
        return commandService.deleteUser(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    @PostMapping("/api/v1/users:onboard")
    public Mono<ResponseEntity<UserResponse>> onboardUser(
            @RequestHeader(value = "x-goog-authenticated-user-email", required = true) String emailHeader,
            @RequestBody UserOnboardRequest req) {
        String parsedEmail = emailHeader.replace("accounts.google.com:", "");
        UserOnboardRequest updatedReq = new UserOnboardRequest(parsedEmail, req.targetLevel(), req.dailyTargetQuestions());
        return commandService.onboardUser(updatedReq)
                .map(this::mapToResponse)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res));
    }

    @PostMapping("/api/v1/users/{userId}:nextQuestion")
    public Mono<ResponseEntity<QuizQuestionResponse>> getNextQuestion(@PathVariable UUID userId) {
        return verifyOwnership(userId).then(queryService.getNextQuestion(userId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/users/{userId}:nextErrorQuestion")
    public Mono<ResponseEntity<QuizQuestionResponse>> getNextErrorQuestion(@PathVariable UUID userId) {
        return verifyOwnership(userId).then(queryService.getNextErrorQuestion(userId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PostMapping("/api/v1/users/{userId}:submitAnswer")
    public Mono<ResponseEntity<SubmitAnswerResponse>> submitAnswer(@PathVariable UUID userId, @RequestBody SubmitAnswerRequest req) {
        return verifyOwnership(userId).then(commandService.submitAnswer(userId, req))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/v1/users:whoami")
    public Mono<ResponseEntity<UserResponse>> whoami(@RequestHeader(value = "x-goog-authenticated-user-email", required = false) String emailHeader) {
        return queryService.whoami(emailHeader)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @GetMapping("/api/v1/users/{userId}:errorReviewCount")
    public Mono<ResponseEntity<ErrorReviewCountResponse>> getErrorReviewCount(@PathVariable UUID userId) {
        return verifyOwnership(userId).then(queryService.getErrorReviewCount(userId))
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().<ErrorReviewCountResponse>build());
    }

    private UserResponse mapToResponse(User entity) {
        return new UserResponse(entity.id(), entity.email(), entity.role() != null ? entity.role().name() : null, entity.targetLevel() != null ? entity.targetLevel().name() : null, entity.learningStreak(), entity.dailyTargetQuestions());
    }

    private Mono<Void> verifyOwnership(UUID targetUserId) {
        return Mono.deferContextual(ctx -> {
            User currentUser = ctx.getOrDefault("CURRENT_USER", null);
            if (currentUser == null) {
                return Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthenticated"));
            }
            if (currentUser.role() != com.vocabest.core.adapter.out.persistence.model.Role.ADMIN && !currentUser.id().equals(targetUserId)) {
                return Mono.error(new org.springframework.web.server.ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied"));
            }
            return Mono.empty();
        });
    }
}
