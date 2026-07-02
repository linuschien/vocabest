Feature: Daily Progress and Streak Tracking Side-Effects
  As a System Domain Logic
  I want to track daily completed questions and update the learning streak
  So that the Learner's progress is accurately maintained

  Scenario Outline: Updating Progress and Recalculating Streak
    Given a Domain logic condition where a "Learner" completes a quiz question
    When the internal progress aggregation process is triggered
    Then verify the side-effect on the "DailyProgressRepositoryAdapter" to increment the daily completed count
    And if it is the first question of the day, verify the streak is incremented

    Examples:
      | learner_id | previous_completed | new_completed | previous_streak | new_streak |
      | ${USER_1}  | 0                  | 1             | 4               | 5          |
      | ${USER_1}  | 5                  | 6             | 5               | 5          |

  Scenario Outline: Midnight Reset of Daily Progress
    Given a Domain logic condition where the system clock crosses midnight
    When the internal daily reset process evaluates the "Learner" progress
    Then verify the side-effect on the "DailyProgressRepositoryAdapter" to initialize a new "DailyProgress" with 0 count
    And if no questions were completed on the previous day, the streak must be reset to 0

    Examples:
      | learner_id | prev_day_completed | expected_streak |
      | ${USER_1}  | 0                  | 0               |
      | ${USER_2}  | 10                 | maintained      |
