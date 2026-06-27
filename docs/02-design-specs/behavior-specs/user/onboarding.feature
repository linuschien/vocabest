Feature: System Cold Start and Data Pipeline (Onboarding)
  As a Learner
  I want to select my TargetLevel and receive static question banks quickly
  So that I get an appropriate and lag-free quiz experience

  Scenario Outline: Dynamic TargetLevel Selection
    Given a new "Learner" accessing the system
    When the "Learner" makes a PUT request to "UserRestController.updateUser" with "<target_level>"
    Then the "Learner" state is updated with the new "TargetLevel"
    And future queries to "QuizQuestionRestController" will filter by "<target_level>"

    Examples:
      | target_level |
      | JUNIOR_HIGH  |
      | SENIOR_HIGH  |

  Scenario Outline: Static Question Bank Delivery
    Given a "Learner" with "TargetLevel" set to "<target_level>"
    When the "Learner" makes a GET request to "QuizQuestionRestController.listQuizQuestions"
    Then the response must contain a "QuizQuestion" list
    And each "QuizQuestion" must include "contextualCloze", "translation", 1 correct option, and 3 distractors
    And the API response time must be less than 200 milliseconds

    Examples:
      | target_level |
      | JUNIOR_HIGH  |
      | SENIOR_HIGH  |
