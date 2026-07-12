package com.vocabest.core.application.port.in;

import com.vocabest.core.adapter.in.web.dto.SubmitAnswerRequest;
import com.vocabest.core.adapter.in.web.dto.SubmitAnswerResponse;
import com.vocabest.core.adapter.in.web.dto.UserOnboardRequest;
import com.vocabest.core.adapter.in.web.dto.UserRequest;
import com.vocabest.core.adapter.out.persistence.model.User;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface UserCommandService {
    Mono<User> createUser(UserRequest req);
    Mono<User> updateUser(UUID id, UserRequest req);
    Mono<User> patchUser(UUID id, com.vocabest.core.adapter.in.web.dto.UserPatchRequest req);
    Mono<Void> deleteUser(UUID id);
    Mono<User> onboardUser(UserOnboardRequest req);
    Mono<SubmitAnswerResponse> submitAnswer(UUID userId, SubmitAnswerRequest req);
}
