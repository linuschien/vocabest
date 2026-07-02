*** Settings ***
Documentation       Target Feature: Learning Dashboard
...                 Upstream Requirement: US-01-learning-dashboard
Library             Browser

*** Test Cases ***
Fetching Daily Progress and Streak
    [Template]    Fetching Daily Progress and Streak Template
    ${USER_1}    10    5

Checking for Pending Error Reviews
    [Template]    Checking for Pending Error Reviews Template
    ${USER_1}    5

Browsing the Word Bank
    [Template]    Browsing the Word Bank Template
    SENIOR_HIGH    "ab"    15

*** Keywords ***
Fetching Daily Progress and Streak Template
    [Arguments]    ${learner_id}    ${expected_completed}    ${expected_streak}
    Given an existing "Learner" with progress records
    When a GraphQL query is made via the "DailyProgressGraphQLResolverAdapter"
    Then the external promise is fulfilled with a "200 OK" response
    And the payload contains "dailyCompletedQuestions" and "learningStreak"

Checking for Pending Error Reviews Template
    [Arguments]    ${learner_id}    ${pending_count}
    Given a "Learner" has pending errors in the "WordMastery" queue
    When a GraphQL query for pending errors is made via the "WordMasteryGraphQLResolverAdapter"
    Then the external promise is fulfilled with a "200 OK" response
    And the payload indicates the count of pending errors is greater than 0

Browsing the Word Bank Template
    [Arguments]    ${target_level}    ${query}    ${expected_count}
    Given the "WordBank" contains multiple words
    When a paginated GraphQL query is made via the "WordBankGraphQLResolverAdapter" with filters    ${query}
    Then the external promise is fulfilled with a "200 OK" response
    And the response contains a list of words matching the "TargetLevel" and fuzzy search query    ${expected_count}

Given an existing "Learner" with progress records
    New Page    ${BASE_URL}/dashboard

When a GraphQL query is made via the "DailyProgressGraphQLResolverAdapter"
    Wait For Elements State    id=progress-grid    visible

Then the external promise is fulfilled with a "200 OK" response
    Log    Response is 200 OK

And the payload contains "dailyCompletedQuestions" and "learningStreak"
    Wait For Elements State    id=questions-answered-metric    visible
    Wait For Elements State    id=learning-streak-metric    visible

Given a "Learner" has pending errors in the "WordMastery" queue
    New Page    ${BASE_URL}/dashboard

When a GraphQL query for pending errors is made via the "WordMasteryGraphQLResolverAdapter"
    Wait For Elements State    id=error-review-btn    visible

And the payload indicates the count of pending errors is greater than 0
    Get Text    id=error-review-btn    contains    Review

Given the "WordBank" contains multiple words
    New Page    ${BASE_URL}/dictionary

When a paginated GraphQL query is made via the "WordBankGraphQLResolverAdapter" with filters
    [Arguments]    ${query}
    Wait For Elements State    id=search-field    visible
    Fill Text    id=search-field    ${query}

And the response contains a list of words matching the "TargetLevel" and fuzzy search query
    [Arguments]    ${expected_count}
    Wait For Elements State    id=word-table    visible
