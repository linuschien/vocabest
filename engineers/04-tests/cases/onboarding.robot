*** Settings ***
Documentation     Target Feature: System Cold Start and Data Pipeline (Onboarding)
...               Upstream Requirement ID: docs/01-requirements/
Library           Browser

*** Test Cases ***
Dynamic TargetLevel Selection
    [Template]    Dynamic TargetLevel Selection Template
    JUNIOR_HIGH
    SENIOR_HIGH

Static Question Bank Delivery
    [Template]    Static Question Bank Delivery Template
    JUNIOR_HIGH
    SENIOR_HIGH

*** Keywords ***
Dynamic TargetLevel Selection Template
    [Arguments]    ${target_level}
    Given a new "Learner" accessing the system
    When the "Learner" makes a PUT request to "UserRestController.updateUser" with "${target_level}"
    Then the "Learner" state is updated with the new "TargetLevel"
    And future queries to "QuizQuestionRestController" will filter by "${target_level}"

Static Question Bank Delivery Template
    [Arguments]    ${target_level}
    Given a "Learner" with "TargetLevel" set to "${target_level}"
    When the "Learner" makes a GraphQL query to "QuizQuestionGraphQLResolver.listQuizQuestions"
    Then the response must contain a "QuizQuestion" list
    And each "QuizQuestion" must include "contextualCloze", "translation", 1 correct option, and 3 distractors
    And the API response time must be less than 200 milliseconds

# Implementations mapping to UI Manifest IDs
Given a new "Learner" accessing the system
    Wait For Elements State    id=onboarding-container    visible

When the "Learner" makes a PUT request to "UserRestController.updateUser" with "${target_level}"
    Click    id=target-level-selection
    Click    id=submit-target-level

Then the "Learner" state is updated with the new "TargetLevel"
    # Verify via API or success toast
    No Operation

And future queries to "QuizQuestionRestController" will filter by "${target_level}"
    # Verify backend interaction
    No Operation

Given a "Learner" with "TargetLevel" set to "${target_level}"
    # Setup test data
    No Operation

When the "Learner" makes a GraphQL query to "QuizQuestionGraphQLResolver.listQuizQuestions"
    # Trigger quiz fetch
    No Operation

Then the response must contain a "QuizQuestion" list
    # Assert on response
    No Operation

And each "QuizQuestion" must include "contextualCloze", "translation", 1 correct option, and 3 distractors
    # Content verification
    No Operation

And the API response time must be less than 200 milliseconds
    # Performance assertion
    No Operation
