Feature: Personalized Error Tracking Side-Effects
  As a System Domain Logic
  I want to record error events and update word mastery asynchronously
  So that the Spaced Repetition queue is properly maintained

  Scenario Outline: Asynchronous Mastery Update on Wrong Answer
    Given a Domain logic condition where a "Learner" answered a question incorrectly
    When the internal error recording process is triggered
    Then verify the side-effect on the "ErrorEventRepositoryAdapter" to persist the new "ErrorEvent"
    And verify the side-effect on the "WordMasteryRepositoryAdapter" to increment the error weight
    And the "SpacedRepetition" algorithm must set a future next review date on the "WordMastery"

    Examples:
      | word_id | initial_weight | expected_weight |
      | ${W_1}  | 0              | 1               |
      | ${W_2}  | 2              | 3               |

  Scenario Outline: Mastery Decay on Correct Answer in Review Mode
    Given a Domain logic condition where an existing "WordMastery" has error weight > 0
    When the internal process receives a correct answer in "ErrorReviewMode"
    Then verify the side-effect on the "WordMasteryRepositoryAdapter" to decrement the error weight
    And the next review date must be cleared if weight reaches 0, or delayed if still > 0

    Examples:
      | word_id | initial_weight | expected_weight | expected_review_date |
      | ${W_1}  | 1              | 0               | null                 |
      | ${W_2}  | 3              | 2               | delayed              |
