package com.vocabest.core.adapter.in.web.rest;

import com.vocabest.core.adapter.in.web.dto.UserRequest;
import com.vocabest.core.adapter.in.web.dto.UserResponse;
import com.vocabest.core.adapter.out.persistence.model.TargetLevel;
import com.vocabest.core.adapter.out.persistence.model.User;
import com.vocabest.core.adapter.out.persistence.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserRestController {

    private final UserRepository repository;

    public UserRestController(UserRepository repository) {
        this.repository = repository;
    }

    @PostMapping
    public Mono<ResponseEntity<UserResponse>> createUser(@RequestBody UserRequest req) {
        User entity = new User(null, TargetLevel.valueOf(req.targetLevel()), req.learningStreak(), req.dailyTargetQuestions(), null, null, null);
        return repository.save(entity)
                .map(this::mapToResponse)
                .map(res -> ResponseEntity.status(HttpStatus.CREATED).body(res));
    }

    @GetMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> getUserById(@PathVariable UUID id) {
        return repository.findById(id)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public Mono<ResponseEntity<UserResponse>> updateUser(@PathVariable UUID id, @RequestBody UserRequest req) {
        return repository.findById(id)
                .map(existing -> new User(existing.id(), TargetLevel.valueOf(req.targetLevel()), req.learningStreak(), req.dailyTargetQuestions(), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(repository::save)
                .map(this::mapToResponse)
                .map(ResponseEntity::ok)
                .defaultIfEmpty(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public Mono<ResponseEntity<Void>> deleteUser(@PathVariable UUID id) {
        return repository.deleteById(id)
                .then(Mono.just(ResponseEntity.noContent().<Void>build()));
    }

    private UserResponse mapToResponse(User entity) {
        return new UserResponse(entity.id(), entity.targetLevel().name(), entity.learningStreak(), entity.dailyTargetQuestions());
    }
}
