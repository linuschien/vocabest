Feature: Learning Dashboard and Home Page
  As a Learner
  I want to see my progress, learning streak, and error review status
  So that I am motivated to learn and can start easily

  Scenario Outline: Fetching Daily Progress and Learning Streak
    Given a "Learner" with ID "<user_id>"
    When the "Learner" makes a GET request to "UserRestController.getUserById"
    And a GraphQL query to "DailyProgressGraphQLResolver.listDailyProgresses"
    Then the response includes the "LearningStreak" value
    And the response includes "completedQuestions" for the current date
    And if the date changed and no questions were completed yesterday, the "LearningStreak" is reset to 0

    Examples:
      | user_id      |
      | ${USER_ID_1} |
      | ${USER_ID_2} |

  Scenario Outline: Error Review Special Mode Entry
    Given a "Learner" with ID "<user_id>"
    When the "Learner" makes a GraphQL query to "ErrorLogGraphQLResolver.listErrorLogs"
    Then the response must return a list of "ErrorLog" items
    And if the pending review count > 0, the UI must provide an entry to "ErrorReviewMode"
    And if the pending review count == 0, the "ErrorReviewMode" entry should be disabled

    Examples:
      | user_id      |
      | ${USER_ID_1} |
      | ${USER_ID_2} |
