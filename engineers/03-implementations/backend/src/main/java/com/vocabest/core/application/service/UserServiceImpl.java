package com.vocabest.core.application.service;

import com.vocabest.core.adapter.in.web.dto.*;
import com.vocabest.core.adapter.out.persistence.model.*;
import com.vocabest.core.adapter.out.persistence.repository.*;
import com.vocabest.core.application.port.in.UserCommandService;
import com.vocabest.core.application.port.in.UserQueryService;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.Random;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserCommandService, UserQueryService {

    private final UserRepository userRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ErrorEventRepository errorEventRepository;
    private final WordMasteryRepository wordMasteryRepository;
    private final Random random = new Random();

    public UserServiceImpl(
            UserRepository userRepository,
            QuizQuestionRepository quizQuestionRepository,
            ErrorEventRepository errorEventRepository,
            WordMasteryRepository wordMasteryRepository) {
        this.userRepository = userRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.errorEventRepository = errorEventRepository;
        this.wordMasteryRepository = wordMasteryRepository;
    }

    @Override
    public Mono<User> getUserById(UUID id) {
        return userRepository.findById(id);
    }

    @Override
    public Flux<User> listUsers(UserFilterInput filter) {
        return userRepository.findAll();
    }

    @Override
    @Transactional
    public Mono<User> createUser(UserRequest req) {
        User entity = new User(null, req.email(), Role.valueOf(req.role()), TargetLevel.valueOf(req.targetLevel()), req.learningStreak(), req.dailyTargetQuestions(), null, null, null);
        return userRepository.save(entity);
    }

    @Override
    @Transactional
    public Mono<User> updateUser(UUID id, UserRequest req) {
        return userRepository.findById(id)
                .map(existing -> new User(existing.id(), req.email(), Role.valueOf(req.role()), TargetLevel.valueOf(req.targetLevel()), req.learningStreak(), req.dailyTargetQuestions(), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(userRepository::save);
    }

    @Override
    @Transactional
    public Mono<Void> deleteUser(UUID id) {
        return userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public Mono<User> onboardUser(UserOnboardRequest req) {
        User probe = new User(null, req.email(), null, null, null, null, null, null, null);
        return userRepository.findOne(Example.of(probe))
                .switchIfEmpty(Mono.defer(() -> {
                    User newUser = new User(null, req.email(), Role.LEARNER, TargetLevel.valueOf(req.targetLevel()), 0, req.dailyTargetQuestions(), null, null, null);
                    return userRepository.save(newUser);
                }));
    }

    @Override
    public Mono<QuizQuestionResponse> getNextQuestion(UUID userId) {
        return userRepository.findById(userId)
                .flatMap(user -> {
                    QuizQuestionFilterInput filter = new QuizQuestionFilterInput(null, null, null, user.targetLevel().name(), null, null);
                    return quizQuestionRepository.count(filter)
                            .flatMap(count -> {
                                if (count == 0) return Mono.empty();
                                int randIndex = random.nextInt(count.intValue());
                                QuizQuestionFilterInput searchFilter = new QuizQuestionFilterInput(null, null, null, user.targetLevel().name(), randIndex, 1);
                                return quizQuestionRepository.search(searchFilter).next();
                            });
                })
                .map(q -> new QuizQuestionResponse(q.id(), q.wordBankId().toString(), q.contextualCloze(), q.chineseTranslation(), q.correctAnswer(), q.distractor1(), q.distractor2(), q.distractor3(), q.explanationRootAffix(), q.explanationMnemonic(), q.createdAt(), q.updatedAt(), q.deletedAt()));
    }

    @Override
    @Transactional
    public Mono<SubmitAnswerResponse> submitAnswer(UUID userId, SubmitAnswerRequest req) {
        return quizQuestionRepository.findById(req.questionId())
                .flatMap(question -> {
                    boolean isCorrect = question.correctAnswer().equals(req.selectedDistractor());
                    
                    Mono<Void> recordError = isCorrect ? Mono.empty() :
                            errorEventRepository.save(new ErrorEvent(null, userId, question.id(), LocalDateTime.now(), req.selectedDistractor(), null, null, null)).then();
                    
                    WordMastery probe = new WordMastery(null, userId, question.wordBankId(), null, null, null, null, null);
                    Mono<Void> updateMastery = wordMasteryRepository.findOne(Example.of(probe))
                            .switchIfEmpty(Mono.defer(() -> Mono.just(new WordMastery(null, userId, question.wordBankId(), 0, LocalDateTime.now(), null, null, null))))
                            .flatMap(mastery -> {
                                int newWeight = Math.max(0, mastery.errorWeight() + (isCorrect ? -1 : 1));
                                WordMastery updated = new WordMastery(mastery.id(), mastery.userId(), mastery.wordBankId(), newWeight, LocalDateTime.now().plusDays(isCorrect ? 3 : 1), mastery.createdAt(), LocalDateTime.now(), mastery.deletedAt());
                                return wordMasteryRepository.save(updated);
                            }).then();

                    return Mono.when(recordError, updateMastery)
                            .then(Mono.just(new SubmitAnswerResponse(isCorrect, question.correctAnswer(), question.explanationRootAffix(), question.explanationMnemonic())));
                });
    }
}
