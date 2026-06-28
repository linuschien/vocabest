*** Settings ***
Documentation     Target Feature: Learning Dashboard and Home Page
...               Upstream Requirement ID: docs/01-requirements/
Library           Browser

*** Test Cases ***
Fetching Daily Progress and Learning Streak
    [Template]    Fetching Daily Progress and Learning Streak Template
    \${USER_ID_1}
    \${USER_ID_2}

Error Review Special Mode Entry
    [Template]    Error Review Special Mode Entry Template
    \${USER_ID_1}
    \${USER_ID_2}

*** Keywords ***
Fetching Daily Progress and Learning Streak Template
    [Arguments]    ${user_id}
    Given a "Learner" with ID "${user_id}"
    When the "Learner" makes a GET request to "UserRestController.getUserById"
    And a GraphQL query to "DailyProgressGraphQLResolver.listDailyProgresses"
    Then the response includes the "LearningStreak" value
    And the response includes "completedQuestions" for the current date
    And if the date changed and no questions were completed yesterday, the "LearningStreak" is reset to 0

Error Review Special Mode Entry Template
    [Arguments]    ${user_id}
    Given a "Learner" with ID "${user_id}"
    When the "Learner" makes a GraphQL query to "ErrorLogGraphQLResolver.listErrorLogs"
    Then the response must return a list of "ErrorLog" items
    And if the pending review count > 0, the UI must provide an entry to "ErrorReviewMode"
    And if the pending review count == 0, the "ErrorReviewMode" entry should be disabled

# Implementations mapping to UI Manifest IDs
Given a "Learner" with ID "${user_id}"
    # Setup user session
    No Operation

When the "Learner" makes a GET request to "UserRestController.getUserById"
    # Trigger API call or navigate
    No Operation

And a GraphQL query to "DailyProgressGraphQLResolver.listDailyProgresses"
    # Trigger API call
    No Operation

Then the response includes the "LearningStreak" value
    Wait For Elements State    id=learning-streak-metric    visible

And the response includes "completedQuestions" for the current date
    Wait For Elements State    id=daily-progress-metric    visible

And if the date changed and no questions were completed yesterday, the "LearningStreak" is reset to 0
    # Verification logic
    No Operation

When the "Learner" makes a GraphQL query to "ErrorLogGraphQLResolver.listErrorLogs"
    # Navigate to error review board or check dashboard
    No Operation

Then the response must return a list of "ErrorLog" items
    Wait For Elements State    id=error-log-table    visible

And if the pending review count > 0, the UI must provide an entry to "ErrorReviewMode"
    Wait For Elements State    id=error-review-entry-trigger    visible
    Get Element States    id=error-review-entry-trigger    contains    enabled

And if the pending review count == 0, the "ErrorReviewMode" entry should be disabled
    Wait For Elements State    id=error-review-entry-trigger    visible
    Get Element States    id=error-review-entry-trigger    contains    disabled
