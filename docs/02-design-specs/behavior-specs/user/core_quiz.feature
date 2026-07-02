Feature: Core Quiz Loop and Instant Feedback
  As a Learner
  I want to fetch quiz questions and receive instant feedback upon answering
  So that I can practice without limits and learn from my mistakes

  Scenario Outline: Fetching Contextual Cloze Questions
    Given the system has available "QuizQuestion" records
    When a "nextQuestion" request is made via the "UserRestControllerAdapter"
    Then the external promise is fulfilled with a "200 OK" response
    And the payload contains the context sentence, translation, and 4 shuffled options

    Examples:
      | target_level | limit |
      | SENIOR_HIGH  | 10    |

  Scenario Outline: Receiving Instant Feedback for Correct Answer
    Given a "QuizQuestion" and the Learner knows the correct option
    When the answer is submitted via the "UserRestControllerAdapter" ("submitAnswer")
    Then the external promise is fulfilled with a "200 OK" response
    And the response indicates the answer is "CORRECT"

    Examples:
      | question_id | answer_submitted | expected_status |
      | ${Q_1}      | "apple"          | "CORRECT"       |

  Scenario Outline: Receiving Instant Feedback and AI Explanation for Wrong Answer
    Given a "QuizQuestion"
    When the wrong answer is submitted via the "UserRestControllerAdapter" ("submitAnswer")
    Then the external promise is fulfilled with a "200 OK" response
    And the response indicates the answer is "WRONG"
    And the response payload includes the "AIExplanationPanel" contents

    Examples:
      | question_id | answer_submitted | expected_status |
      | ${Q_1}      | "banana"         | "WRONG"         |
