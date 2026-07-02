Feature: Learning Dashboard
  As a Learner
  I want to view my progress, streak, and access error reviews or the word bank
  So that I can smoothly start my daily practice

  Scenario Outline: Fetching Daily Progress and Streak
    Given an existing "Learner" with progress records
    When a GET request is made via the "DailyProgressRestControllerAdapter"
    Then the external promise is fulfilled with a "200 OK" response
    And the payload contains "dailyCompletedQuestions" and "learningStreak"

    Examples:
      | learner_id | expected_completed | expected_streak |
      | ${USER_1}  | 10                 | 5               |

  Scenario Outline: Checking for Pending Error Reviews
    Given a "Learner" has pending errors in the "WordMastery" queue
    When a request for pending errors is made via the "ErrorEventRestControllerAdapter"
    Then the external promise is fulfilled with a "200 OK" response
    And the payload indicates the count of pending errors is greater than 0

    Examples:
      | learner_id | pending_count |
      | ${USER_1}  | 5             |

  Scenario Outline: Browsing the Word Bank
    Given the "WordBank" contains multiple words
    When a paginated GET request is made via the "WordBankRestControllerAdapter" with filters
    Then the external promise is fulfilled with a "200 OK" response
    And the response contains a list of words matching the "TargetLevel" and fuzzy search query

    Examples:
      | target_level | query | expected_count |
      | SENIOR_HIGH  | "ab"  | 15             |
