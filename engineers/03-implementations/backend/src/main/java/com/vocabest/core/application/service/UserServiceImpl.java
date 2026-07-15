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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class UserServiceImpl implements UserCommandService, UserQueryService {

    private static final Logger log = LoggerFactory.getLogger(UserServiceImpl.class);


    private final UserRepository userRepository;
    private final QuizQuestionRepository quizQuestionRepository;
    private final ErrorEventRepository errorEventRepository;
    private final WordMasteryRepository wordMasteryRepository;
    private final DailyProgressRepository dailyProgressRepository;
    private final WordBankRepository wordBankRepository;
    private final Random random = new Random();

    @org.springframework.beans.factory.annotation.Value("${vocabest.admin.whitelist:}")
    private java.util.List<String> adminWhitelist;

    public UserServiceImpl(
            UserRepository userRepository,
            QuizQuestionRepository quizQuestionRepository,
            ErrorEventRepository errorEventRepository,
            WordMasteryRepository wordMasteryRepository,
            DailyProgressRepository dailyProgressRepository,
            WordBankRepository wordBankRepository) {
        this.userRepository = userRepository;
        this.quizQuestionRepository = quizQuestionRepository;
        this.errorEventRepository = errorEventRepository;
        this.wordMasteryRepository = wordMasteryRepository;
        this.dailyProgressRepository = dailyProgressRepository;
        this.wordBankRepository = wordBankRepository;
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
        User entity = new User(null, req.email(), Role.valueOf(req.role()), TargetLevel.valueOf(req.targetLevel()), req.learningStreak(), req.learningStreak(), 0, req.dailyTargetQuestions(), null, null, null);
        return userRepository.save(entity);
    }

    @Override
    @Transactional
    public Mono<User> updateUser(UUID id, UserRequest req) {
        return userRepository.findById(id)
                .map(existing -> new User(existing.id(), req.email(), Role.valueOf(req.role()), TargetLevel.valueOf(req.targetLevel()), req.learningStreak(), Math.max(existing.maxLearningStreak() != null ? existing.maxLearningStreak() : 0, req.learningStreak()), existing.maxDailyQuestions(), req.dailyTargetQuestions(), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
                .flatMap(userRepository::save);
    }

    @Override
    @Transactional
    public Mono<User> patchUser(UUID id, UserPatchRequest req) {
        return userRepository.findById(id)
                .map(existing -> new User(existing.id(), existing.email(), existing.role(), TargetLevel.valueOf(req.targetLevel()), existing.learningStreak(), existing.maxLearningStreak(), existing.maxDailyQuestions(), req.dailyTargetQuestions(), existing.createdAt(), LocalDateTime.now(), existing.deletedAt()))
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
        log.info("Onboarding user with email: {}", req.email());
        User probe = new User(null, req.email(), null, null, null, null, null, null, null, null, null);
        return userRepository.findOne(Example.of(probe))
                .switchIfEmpty(Mono.defer(() -> {
                    Role role = adminWhitelist != null && adminWhitelist.contains(req.email()) ? Role.ADMIN : Role.LEARNER;
                    User newUser = new User(null, req.email(), role, TargetLevel.valueOf(req.targetLevel()), 0, 0, 0, req.dailyTargetQuestions(), LocalDateTime.now(), LocalDateTime.now(), null);
                    return userRepository.save(newUser);
                }));
    }

    @Override
    public Mono<QuizQuestionResponse> getNextQuestion(UUID userId) {
        log.info("Fetching next question for user: {}", userId);
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
                .map(q -> new QuizQuestionResponse(q.id(), q.wordBankId().toString(), q.contextualCloze(), q.chineseTranslation(), q.correctAnswer(), q.distractor1(), q.distractor2(), q.distractor3(), q.explanationRootAffix(), q.explanationMnemonic()));
    }

    @Override
    public Mono<QuizQuestionResponse> getNextErrorQuestion(UUID userId) {
        log.info("Fetching next error review question for user: {}", userId);
        return wordMasteryRepository.findFirstByUserIdAndNextReviewDateLessThanEqualAndErrorWeightGreaterThanOrderByErrorWeightDescNextReviewDateAsc(userId, LocalDateTime.now(), 0)
                .flatMap(mastery -> quizQuestionRepository.findErrorQuestionsByUserAndWordBank(userId, mastery.wordBankId())
                        .collectList()
                        .flatMap(errorQuestions -> {
                            if (!errorQuestions.isEmpty()) {
                                return Mono.just(errorQuestions.get(random.nextInt(errorQuestions.size())));
                            }
                            return quizQuestionRepository.findByWordBankId(mastery.wordBankId())
                                    .collectList()
                                    .flatMap(questions -> {
                                        if (questions.isEmpty()) return Mono.empty();
                                        return Mono.just(questions.get(random.nextInt(questions.size())));
                                    });
                        })
                )
                .map(q -> new QuizQuestionResponse(q.id(), q.wordBankId().toString(), q.contextualCloze(), q.chineseTranslation(), q.correctAnswer(), q.distractor1(), q.distractor2(), q.distractor3(), q.explanationRootAffix(), q.explanationMnemonic()));
    }

    @Override
    public Mono<WordBankResponse> getWordleTarget(UUID userId) {
        return userRepository.findById(userId)
                .flatMap(user -> wordBankRepository.findRandomWordleTarget(user.targetLevel().name()))
                .map(wb -> new WordBankResponse(
                        wb.id(),
                        wb.word(),
                        wb.partsOfSpeech(),
                        wb.chineseTranslation(),
                        wb.targetLevel() != null ? wb.targetLevel().name() : null,
                        wb.difficultyLevel(),
                        wb.examFrequency()
                ));
    }

    @Override
    public Mono<WordleValidationResponse> validateWordleGuess(UUID userId, String guess) {
        if (guess == null || !guess.matches("^[a-zA-Z]{5}$")) {
            return Mono.just(new WordleValidationResponse(false));
        }
        return userRepository.findById(userId)
                .flatMap(user -> wordBankRepository.existsByWordAndTargetLevel(guess, user.targetLevel()))
                .map(WordleValidationResponse::new)
                .defaultIfEmpty(new WordleValidationResponse(false));
    }
    
    @Override
    public Flux<WordBankResponse> getCrosswordTargets(UUID userId, int count) {
        return userRepository.findById(userId)
                .flatMapMany(user -> wordBankRepository.findRandomCrosswordTargets(
                        user.targetLevel() != null ? user.targetLevel().name() : null, count))
                .map(wb -> new WordBankResponse(
                        wb.id(),
                        wb.word(),
                        wb.partsOfSpeech(),
                        wb.chineseTranslation(),
                        wb.targetLevel() != null ? wb.targetLevel().name() : null,
                        wb.difficultyLevel(),
                        wb.examFrequency()
                ));
    }

    @Override
    public Mono<User> whoami() {
        return Mono.deferContextual(ctx -> {
            String parsedEmail = ctx.getOrDefault("CURRENT_EMAIL", null);
            if (parsedEmail == null) return Mono.empty();
            
            User user = ctx.getOrDefault("CURRENT_USER", null);
            if (user != null) {
                log.info("Handling whoami for authenticated user: {}", user.email());
                java.time.LocalDate today = java.time.LocalDate.now();
                return dailyProgressRepository.findByUserIdAndDate(user.id(), today)
                        .switchIfEmpty(Mono.just(new DailyProgress(null, user.id(), today, user.dailyTargetQuestions(), 0, 0, 0, null, null, null)))
                        .flatMap(todayProgress -> {
                            if (todayProgress.answeredQuestions() > 0) {
                                return Mono.just(user);
                            }
                            return dailyProgressRepository.findByUserIdAndDate(user.id(), today.minusDays(1))
                                    .switchIfEmpty(Mono.just(new DailyProgress(null, user.id(), today.minusDays(1), user.dailyTargetQuestions(), 0, 0, 0, null, null, null)))
                                    .flatMap(yesterdayProgress -> {
                                        if (yesterdayProgress.answeredQuestions() == 0 && user.learningStreak() > 0) {
                                            User resetUser = new User(user.id(), user.email(), user.role(), user.targetLevel(), 0, user.maxLearningStreak(), user.maxDailyQuestions(), user.dailyTargetQuestions(), user.createdAt(), LocalDateTime.now(), user.deletedAt());
                                            return userRepository.save(resetUser);
                                        }
                                        return Mono.just(user);
                                    });
                        });
            }
            return Mono.just(new User(null, parsedEmail, null, null, null, null, null, null, null, null, null));
        });
    }

    @Override
    public Mono<ErrorReviewCountResponse> getErrorReviewCount(UUID userId) {
        return wordMasteryRepository.countByUserIdAndNextReviewDateLessThanEqualAndErrorWeightGreaterThan(userId, LocalDateTime.now(), 0)
                .map(ErrorReviewCountResponse::new);
    }

    @Override
    @Transactional
    public Mono<SubmitAnswerResponse> submitAnswer(UUID userId, SubmitAnswerRequest req) {
        log.info("Processing submitAnswer for user: {}, questionId: {}, selected: {}", userId, req.questionId(), req.selectedDistractor());
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
                                LocalDateTime nextReview = null;
                                if (newWeight > 0) {
                                    nextReview = LocalDateTime.now().plusDays(isCorrect ? 3 : 1);
                                }
                                WordMastery updated = new WordMastery(mastery.id(), mastery.userId(), mastery.wordBankId(), newWeight, nextReview, mastery.createdAt(), LocalDateTime.now(), mastery.deletedAt());
                                return wordMasteryRepository.save(updated);
                            }).then();

                    Mono<Void> updateProgress = userRepository.findById(userId)
                            .flatMap(user -> {
                                java.time.LocalDate today = java.time.LocalDate.now();
                                return dailyProgressRepository.findByUserIdAndDate(userId, today)
                                        .switchIfEmpty(Mono.just(new DailyProgress(null, userId, today, user.dailyTargetQuestions(), 0, 0, 0, null, null, null)))
                                        .flatMap(todayProgress -> {
                                            Mono<User> userUpdate = Mono.just(user);
                                            int currentAnsweredToday = todayProgress.answeredQuestions() + 1;
                                            
                                            if (todayProgress.answeredQuestions() == 0) {
                                                userUpdate = dailyProgressRepository.findByUserIdAndDate(userId, today.minusDays(1))
                                                        .switchIfEmpty(Mono.just(new DailyProgress(null, userId, today.minusDays(1), user.dailyTargetQuestions(), 0, 0, 0, null, null, null)))
                                                        .flatMap(yesterdayProgress -> {
                                                            int newStreak = yesterdayProgress.answeredQuestions() > 0 ? user.learningStreak() + 1 : 1;
                                                            int newMaxStreak = Math.max(newStreak, user.maxLearningStreak() != null ? user.maxLearningStreak() : 0);
                                                            int newMaxDaily = Math.max(currentAnsweredToday, user.maxDailyQuestions() != null ? user.maxDailyQuestions() : 0);
                                                            User updatedUser = new User(user.id(), user.email(), user.role(), user.targetLevel(), newStreak, newMaxStreak, newMaxDaily, user.dailyTargetQuestions(), user.createdAt(), LocalDateTime.now(), user.deletedAt());
                                                            return userRepository.save(updatedUser);
                                                        });
                                            } else {
                                                int oldMaxDaily = user.maxDailyQuestions() != null ? user.maxDailyQuestions() : 0;
                                                if (currentAnsweredToday > oldMaxDaily) {
                                                    User updatedUser = new User(user.id(), user.email(), user.role(), user.targetLevel(), user.learningStreak(), user.maxLearningStreak(), currentAnsweredToday, user.dailyTargetQuestions(), user.createdAt(), LocalDateTime.now(), user.deletedAt());
                                                    userUpdate = userRepository.save(updatedUser);
                                                }
                                            }
                                            DailyProgress updatedProgress = new DailyProgress(todayProgress.id(), userId, today, todayProgress.targetQuestions(), todayProgress.answeredQuestions() + 1, todayProgress.correctQuestions() + (isCorrect ? 1 : 0), todayProgress.wrongQuestions() + (isCorrect ? 0 : 1), todayProgress.createdAt(), LocalDateTime.now(), todayProgress.deletedAt());
                                            return userUpdate.then(dailyProgressRepository.save(updatedProgress));
                                        });
                            }).then();

                    return Mono.when(recordError, updateMastery, updateProgress)
                            .then(Mono.just(new SubmitAnswerResponse(isCorrect, question.correctAnswer(), question.explanationRootAffix(), question.explanationMnemonic())));
                });
    }
}
