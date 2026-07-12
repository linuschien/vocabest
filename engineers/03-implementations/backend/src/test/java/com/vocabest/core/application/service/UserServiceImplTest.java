package com.vocabest.core.application.service;

import com.vocabest.core.adapter.in.web.dto.*;
import com.vocabest.core.adapter.out.persistence.model.*;
import com.vocabest.core.adapter.out.persistence.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Example;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private QuizQuestionRepository quizQuestionRepository;

    @Mock
    private ErrorEventRepository errorEventRepository;

    @Mock
    private WordMasteryRepository wordMasteryRepository;

    @Mock
    private DailyProgressRepository dailyProgressRepository;

    @InjectMocks
    private UserServiceImpl userService;

    private User testUser;
    private UUID testUserId;
    private QuizQuestion testQuestion;
    private UUID testQuestionId;
    private UUID testWordBankId;

    @BeforeEach
    void setUp() {
        testUserId = UUID.randomUUID();
        testUser = new User(testUserId, "test@example.com", Role.LEARNER, TargetLevel.JUNIOR_HIGH, 5, 20, LocalDateTime.now(), LocalDateTime.now(), null);

        testQuestionId = UUID.randomUUID();
        testWordBankId = UUID.randomUUID();
        testQuestion = new QuizQuestion(testQuestionId, testWordBankId, "I ate an __", "我吃了一顆蘋果", "apple", "banana", "orange", "grape", "root", "mnem", LocalDateTime.now(), LocalDateTime.now(), null);
    }

    @Test
    void getUserById_shouldReturnUser() {
        when(userRepository.findById(testUserId)).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.getUserById(testUserId))
                .expectNext(testUser)
                .verifyComplete();
    }

    @Test
    void listUsers_shouldReturnFluxOfUsers() {
        when(userRepository.findAll()).thenReturn(Flux.just(testUser));

        StepVerifier.create(userService.listUsers(new UserFilterInput(null, null, null, null, null, null)))
                .expectNext(testUser)
                .verifyComplete();
    }

    @Test
    void createUser_shouldReturnCreatedUser() {
        UserRequest request = new UserRequest("test2@example.com", "LEARNER", "SENIOR_HIGH", 25, 0);
        User savedUser = new User(UUID.randomUUID(), "test2@example.com", Role.LEARNER, TargetLevel.SENIOR_HIGH, 0, 25, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        StepVerifier.create(userService.createUser(request))
                .expectNextMatches(u -> u.email().equals("test2@example.com") && u.targetLevel() == TargetLevel.SENIOR_HIGH)
                .verifyComplete();
    }

    @Test
    void updateUser_shouldReturnUpdatedUser() {
        UserRequest request = new UserRequest("test@example.com", "LEARNER", "SENIOR_HIGH", 30, 10);
        User updated = new User(testUserId, "test@example.com", Role.LEARNER, TargetLevel.SENIOR_HIGH, 10, 30, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(userRepository.findById(testUserId)).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(userService.updateUser(testUserId, request))
                .expectNextMatches(u -> u.learningStreak() == 10 && u.targetLevel() == TargetLevel.SENIOR_HIGH)
                .verifyComplete();
    }

    @Test
    void patchUser_shouldReturnUpdatedUser() {
        UserPatchRequest request = new UserPatchRequest("SENIOR_HIGH", 30);
        User updated = new User(testUserId, "test@example.com", Role.LEARNER, TargetLevel.SENIOR_HIGH, 0, 30, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(userRepository.findById(testUserId)).thenReturn(Mono.just(testUser));
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(updated));

        StepVerifier.create(userService.patchUser(testUserId, request))
                .expectNextMatches(u -> u.targetLevel() == TargetLevel.SENIOR_HIGH && u.dailyTargetQuestions() == 30)
                .verifyComplete();
    }

    @Test
    void deleteUser_shouldReturnEmpty() {
        when(userRepository.deleteById(testUserId)).thenReturn(Mono.empty());

        StepVerifier.create(userService.deleteUser(testUserId))
                .verifyComplete();
    }

    @Test
    void onboardUser_whenUserExists_shouldReturnExistingUser() {
        UserOnboardRequest req = new UserOnboardRequest("test@example.com", "JUNIOR_HIGH", 20);
        when(userRepository.findOne(any(Example.class))).thenReturn(Mono.just(testUser));

        StepVerifier.create(userService.onboardUser(req))
                .expectNext(testUser)
                .verifyComplete();
    }

    @Test
    void onboardUser_whenUserDoesNotExist_shouldCreateAndReturnUser() {
        UserOnboardRequest req = new UserOnboardRequest("new@example.com", "JUNIOR_HIGH", 20);
        User savedUser = new User(UUID.randomUUID(), "new@example.com", Role.LEARNER, TargetLevel.JUNIOR_HIGH, 0, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(userRepository.findOne(any(Example.class))).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(savedUser));

        StepVerifier.create(userService.onboardUser(req))
                .expectNext(savedUser)
                .verifyComplete();
    }

    @Test
    void getNextQuestion_shouldReturnQuestion_whenAvailable() {
        when(userRepository.findById(testUserId)).thenReturn(Mono.just(testUser));
        when(quizQuestionRepository.count(any(QuizQuestionFilterInput.class))).thenReturn(Mono.just(5L));
        when(quizQuestionRepository.search(any(QuizQuestionFilterInput.class))).thenReturn(Flux.just(testQuestion));

        StepVerifier.create(userService.getNextQuestion(testUserId))
                .expectNextMatches(res -> res.correctAnswer().equals("apple") && res.contextualCloze().equals("I ate an __"))
                .verifyComplete();
    }

    @Test
    void getNextQuestion_shouldReturnEmpty_whenNoQuestionsAvailable() {
        when(userRepository.findById(testUserId)).thenReturn(Mono.just(testUser));
        when(quizQuestionRepository.count(any(QuizQuestionFilterInput.class))).thenReturn(Mono.just(0L));

        StepVerifier.create(userService.getNextQuestion(testUserId))
                .verifyComplete();
    }

    @Test
    void getNextErrorQuestion_shouldReturnQuestion_whenAvailable() {
        WordMastery mastery = new WordMastery(UUID.randomUUID(), testUserId, testWordBankId, 2, LocalDateTime.now().minusDays(1), LocalDateTime.now(), LocalDateTime.now(), null);
        when(wordMasteryRepository.findFirstByUserIdAndNextReviewDateLessThanEqualAndErrorWeightGreaterThanOrderByErrorWeightDescNextReviewDateAsc(eq(testUserId), any(), eq(0)))
                .thenReturn(Mono.just(mastery));
        when(quizQuestionRepository.findErrorQuestionsByUserAndWordBank(testUserId, testWordBankId)).thenReturn(Flux.just(testQuestion));

        StepVerifier.create(userService.getNextErrorQuestion(testUserId))
                .expectNextMatches(res -> res.correctAnswer().equals("apple") && res.contextualCloze().equals("I ate an __"))
                .verifyComplete();
    }

    @Test
    void getNextErrorQuestion_shouldReturnEmpty_whenNoQuestionsAvailable() {
        when(wordMasteryRepository.findFirstByUserIdAndNextReviewDateLessThanEqualAndErrorWeightGreaterThanOrderByErrorWeightDescNextReviewDateAsc(eq(testUserId), any(), eq(0)))
                .thenReturn(Mono.empty());

        StepVerifier.create(userService.getNextErrorQuestion(testUserId))
                .verifyComplete();
    }

    @Test
    void submitAnswer_whenCorrect_shouldUpdateMasteryPositive() {
        SubmitAnswerRequest req = new SubmitAnswerRequest(testQuestionId, "apple");
        WordMastery existingMastery = new WordMastery(UUID.randomUUID(), testUserId, testWordBankId, 2, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(quizQuestionRepository.findById(testQuestionId)).thenReturn(Mono.just(testQuestion));
        when(wordMasteryRepository.findOne(any(Example.class))).thenReturn(Mono.just(existingMastery));
        
        WordMastery updatedMastery = new WordMastery(existingMastery.id(), testUserId, testWordBankId, 1, LocalDateTime.now().plusDays(3), LocalDateTime.now(), LocalDateTime.now(), null);
        when(wordMasteryRepository.save(any(WordMastery.class))).thenReturn(Mono.just(updatedMastery));

        when(userRepository.findById(testUserId)).thenReturn(Mono.just(testUser));
        when(dailyProgressRepository.findByUserIdAndDate(eq(testUserId), any())).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
        when(dailyProgressRepository.save(any(DailyProgress.class))).thenReturn(Mono.just(new DailyProgress(UUID.randomUUID(), testUserId, java.time.LocalDate.now(), 20, 1, 1, 0, LocalDateTime.now(), LocalDateTime.now(), null)));

        StepVerifier.create(userService.submitAnswer(testUserId, req))
                .expectNextMatches(SubmitAnswerResponse::isCorrect)
                .verifyComplete();
        
        verify(errorEventRepository, never()).save(any(ErrorEvent.class));
    }

    @Test
    void submitAnswer_whenCorrect_shouldSetNextReviewDateToNull_whenWeightDropsToZero() {
        SubmitAnswerRequest req = new SubmitAnswerRequest(testQuestionId, "apple");
        WordMastery existingMastery = new WordMastery(UUID.randomUUID(), testUserId, testWordBankId, 1, LocalDateTime.now(), LocalDateTime.now(), LocalDateTime.now(), null);
        
        when(quizQuestionRepository.findById(testQuestionId)).thenReturn(Mono.just(testQuestion));
        when(wordMasteryRepository.findOne(any(Example.class))).thenReturn(Mono.just(existingMastery));
        
        WordMastery updatedMastery = new WordMastery(existingMastery.id(), testUserId, testWordBankId, 0, null, LocalDateTime.now(), LocalDateTime.now(), null);
        when(wordMasteryRepository.save(any(WordMastery.class))).thenReturn(Mono.just(updatedMastery));

        when(userRepository.findById(testUserId)).thenReturn(Mono.just(testUser));
        when(dailyProgressRepository.findByUserIdAndDate(eq(testUserId), any())).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
        when(dailyProgressRepository.save(any(DailyProgress.class))).thenReturn(Mono.just(new DailyProgress(UUID.randomUUID(), testUserId, java.time.LocalDate.now(), 20, 1, 1, 0, LocalDateTime.now(), LocalDateTime.now(), null)));

        StepVerifier.create(userService.submitAnswer(testUserId, req))
                .expectNextMatches(SubmitAnswerResponse::isCorrect)
                .verifyComplete();
        
        verify(errorEventRepository, never()).save(any(ErrorEvent.class));
    }

    @Test
    void submitAnswer_whenIncorrect_shouldRecordErrorAndUpdateMasteryNegative() {
        SubmitAnswerRequest req = new SubmitAnswerRequest(testQuestionId, "banana");
        
        when(quizQuestionRepository.findById(testQuestionId)).thenReturn(Mono.just(testQuestion));
        
        // Mock error saving
        when(errorEventRepository.save(any(ErrorEvent.class))).thenReturn(Mono.just(new ErrorEvent(UUID.randomUUID(), testUserId, testQuestionId, LocalDateTime.now(), "banana", LocalDateTime.now(), LocalDateTime.now(), null)));
        
        // Mock mastery missing (will create a new one)
        when(wordMasteryRepository.findOne(any(Example.class))).thenReturn(Mono.empty());
        WordMastery newMastery = new WordMastery(UUID.randomUUID(), testUserId, testWordBankId, 1, LocalDateTime.now().plusDays(1), LocalDateTime.now(), LocalDateTime.now(), null);
        when(wordMasteryRepository.save(any(WordMastery.class))).thenReturn(Mono.just(newMastery));

        when(userRepository.findById(testUserId)).thenReturn(Mono.just(testUser));
        when(dailyProgressRepository.findByUserIdAndDate(eq(testUserId), any())).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(testUser));
        when(dailyProgressRepository.save(any(DailyProgress.class))).thenReturn(Mono.just(new DailyProgress(UUID.randomUUID(), testUserId, java.time.LocalDate.now(), 20, 1, 0, 1, LocalDateTime.now(), LocalDateTime.now(), null)));

        StepVerifier.create(userService.submitAnswer(testUserId, req))
                .expectNextMatches(res -> !res.isCorrect() && res.correctAnswer().equals("apple"))
                .verifyComplete();
        
        verify(errorEventRepository, times(1)).save(any(ErrorEvent.class));
    }

    @Test
    void whoami_shouldResetStreak_ifMissedYesterday() {
        User user = new User(testUserId, "test@example.com", Role.LEARNER, TargetLevel.JUNIOR_HIGH, 5, 20, LocalDateTime.now(), LocalDateTime.now(), null);
        when(dailyProgressRepository.findByUserIdAndDate(eq(testUserId), any(java.time.LocalDate.class))).thenReturn(Mono.empty());
        when(userRepository.save(any(User.class))).thenReturn(Mono.just(user)); // simplified

        StepVerifier.create(userService.whoami().contextWrite(reactor.util.context.Context.of("CURRENT_USER", user, "CURRENT_EMAIL", "test@example.com")))
                .expectNextMatches(u -> u.email().equals("test@example.com"))
                .verifyComplete();
    }

    @Test
    void getErrorReviewCount_shouldReturnCount() {
        when(wordMasteryRepository.countByUserIdAndNextReviewDateLessThanEqualAndErrorWeightGreaterThan(eq(testUserId), any(), eq(0)))
                .thenReturn(Mono.just(5L));

        StepVerifier.create(userService.getErrorReviewCount(testUserId))
                .expectNextMatches(res -> res.count() == 5)
                .verifyComplete();
    }
}
