Feature: Learner Onboarding
  As a System and Learner
  I want to automatically create a profile on first login and select target levels
  So that the Learner can start using the system smoothly

  Scenario Outline: First Login and Automatic Registration
    Given a new user who has just passed authentication
    When the "Learner" submits initial settings via the "UserRestControllerAdapter"
    Then the "vocabest-core-api" should fulfill the promise by returning a "201 Created" status
    And the user profile should contain the selected "TargetLevel" and "DailyTargetQuestions"

    Examples:
      | user_email           | target_level | daily_target |
      | new_user@example.com | SENIOR_HIGH  | 20           |
      | junior@example.com   | JUNIOR_HIGH  | 10           |

  Scenario Outline: Updating Target Settings
    Given an existing "Learner" profile
    When the "Learner" updates their settings via the "UserRestControllerAdapter"
    Then the "vocabest-core-api" should fulfill the promise by returning a "200 OK" status
    And the returned profile must reflect the new "TargetLevel"

    Examples:
      | user_email       | new_target_level |
      | test@example.com | SENIOR_HIGH      |
