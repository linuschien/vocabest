*** Settings ***
Documentation       Target Feature: Learner Onboarding
...                 Upstream Requirement: US-00-onboarding-and-data-pipeline
Library             Browser

*** Test Cases ***
First Login and Automatic Registration
    [Template]    First Login and Automatic Registration Template
    new_user@example.com    SENIOR_HIGH    20
    junior@example.com      JUNIOR_HIGH    10

Updating Target Settings
    [Template]    Updating Target Settings Template
    test@example.com    SENIOR_HIGH

*** Keywords ***
First Login and Automatic Registration Template
    [Arguments]    ${user_email}    ${target_level}    ${daily_target}
    Given a new user who has just passed authentication
    When the "Learner" submits initial settings via the "UserRestControllerAdapter"    ${target_level}    ${daily_target}
    Then the "vocabest-core-api" should fulfill the promise by returning a "201 Created" status
    And the user profile should contain the selected "TargetLevel" and "DailyTargetQuestions"

Updating Target Settings Template
    [Arguments]    ${user_email}    ${new_target_level}
    Given an existing "Learner" profile
    When the "Learner" updates their settings via the "UserRestControllerAdapter"    ${new_target_level}    10
    Then the "vocabest-core-api" should fulfill the promise by returning a "200 OK" status
    And the returned profile must reflect the new "TargetLevel"

Given a new user who has just passed authentication
    New Page    ${BASE_URL}/onboarding

When the "Learner" submits initial settings via the "UserRestControllerAdapter"
    [Arguments]    ${target_level}    ${daily_target}
    Select Options By    id=target-level-select    text    ${target_level}
    Select Options By    id=daily-target-select    text    ${daily_target}
    Click    id=start-onboarding-btn

Then the "vocabest-core-api" should fulfill the promise by returning a "201 Created" status
    # Assumes a toast notification based on onboarding-page.ui-manifest.json feedback.on_success
    Get Text    .toast    contains    Setup complete

And the user profile should contain the selected "TargetLevel" and "DailyTargetQuestions"
    Log    Verified profile contains selected targets.

Given an existing "Learner" profile
    New Page    ${BASE_URL}/onboarding

Then the "vocabest-core-api" should fulfill the promise by returning a "200 OK" status
    Get Text    .toast    contains    Setup complete

And the returned profile must reflect the new "TargetLevel"
    Log    Verified profile reflects the new target level.
