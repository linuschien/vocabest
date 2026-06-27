Feature: Error Tracking and Spaced Repetition Scheduling
  As a System
  I want to automatically record errors and schedule spaced repetitions
  So that the Learner can review words efficiently at the optimal time on the forgetting curve

  Scenario Outline: Recording a Failure and Scheduling Review
    Given a Domain logic condition where a failure is reported for "<word_id>"
    When the internal process "ErrorLogRestController.recordFailure" is triggered
    Then the "ErrorLogRepository" should persist the updated record
    And the "errorWeight" for the word should be incremented
    And the "SpacedRepetition" algorithm should calculate and set the "nextReviewDate"

    Examples:
      | word_id     | initial_weight | new_weight |
      | ${WORD_ID_1}| 0              | 1          |
      | ${WORD_ID_2}| 2              | 3          |

  Scenario Outline: Fetching Error Review Queue
    Given a Domain logic condition where a "Learner" requests their error review queue
    When the internal query is processed by the "ErrorLogRepository"
    Then the returned list must filter for "nextReviewDate" that is today or in the past
    And the list must be sorted by "errorWeight" descending

    Examples:
      | learner_id |
      | ${USER_ID} |

  Scenario Outline: Correcting an Error in Review Mode
    Given an existing "ErrorLog" for "<word_id>" with "errorWeight" > 0
    When the "Learner" answers correctly in "ErrorReviewMode"
    Then the "ErrorLogRepository" should persist the update
    And the "errorWeight" should be decreased or appropriately adjusted
    And the "nextReviewDate" should be delayed to a future date

    Examples:
      | word_id    | initial_weight |
      | ${WORD_ID} | 3              |
