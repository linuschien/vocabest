Feature: Core Quiz Loop and Instant Feedback
  As a Learner
  I want an immersive, uninterrupted quiz experience with instant feedback
  So that I can learn effectively and maintain my flow state

  Scenario Outline: Rendering Contextual Cloze Questions
    Given a "Learner" receives a "QuizQuestion" from "QuizQuestionRestController"
    When the quiz UI renders the question
    Then the screen must display the full Chinese "translation"
    And the screen must display the "ContextualCloze" English sentence
    And the screen must provide 4 randomized option buttons containing 1 "correctOption" and 3 distractors

    Examples:
      | learner_state |
      | practicing    |

  Scenario Outline: Unlimited Practice and Positive Reinforcement
    Given a "Learner" viewing a "QuizQuestion"
    When the "Learner" selects the "correctOption"
    Then the selected button indicates success
    And the system automatically transitions to the next question after 1.5 seconds without deducting health
    And a POST request is made to update "DailyProgressRestController" if applicable

    Examples:
      | selected_option | result  |
      | correctOption   | success |

  Scenario Outline: AI Explanation Panel on Incorrect Answer
    Given a "Learner" viewing a "QuizQuestion" with ID "<word_id>"
    When the "Learner" selects a "distractor"
    Then the selected button indicates an error (turns red)
    And the "correctOption" button is revealed (turns green)
    And the "AIExplanationPanel" appears showing "explanationRootAffix" and "explanationMnemonic"
    And the quiz flow pauses until the "Learner" manually clicks next
    And a POST request is made to "ErrorLogRestController.recordFailure" to log the error for "<word_id>"

    Examples:
      | selected_option | word_id     |
      | distractor1     | ${WORD_ID}  |
      | distractor2     | ${WORD_ID}  |
      | distractor3     | ${WORD_ID}  |
