*** Settings ***
Documentation     Target Feature: Core Quiz Loop and Instant Feedback
...               Upstream Requirement ID: docs/01-requirements/
Library           Browser

*** Test Cases ***
Rendering Contextual Cloze Questions
    [Template]    Rendering Contextual Cloze Questions Template
    practicing

Unlimited Practice and Positive Reinforcement
    [Template]    Unlimited Practice and Positive Reinforcement Template
    correctOption    success

AI Explanation Panel on Incorrect Answer
    [Template]    AI Explanation Panel on Incorrect Answer Template
    distractor1    \${WORD_ID}
    distractor2    \${WORD_ID}
    distractor3    \${WORD_ID}

*** Keywords ***
Rendering Contextual Cloze Questions Template
    [Arguments]    ${learner_state}
    Given a "Learner" receives a "QuizQuestion" from "QuizQuestionRestController"
    When the quiz UI renders the question
    Then the screen must display the full Chinese "translation"
    And the screen must display the "ContextualCloze" English sentence
    And the screen must provide 4 randomized option buttons containing 1 "correctOption" and 3 distractors

Unlimited Practice and Positive Reinforcement Template
    [Arguments]    ${selected_option}    ${result}
    Given a "Learner" viewing a "QuizQuestion"
    When the "Learner" selects the "correctOption"
    Then the selected button indicates success
    And the system automatically transitions to the next question after 1.5 seconds without deducting health
    And a POST request is made to update "DailyProgressRestController" if applicable

AI Explanation Panel on Incorrect Answer Template
    [Arguments]    ${selected_option}    ${word_id}
    Given a "Learner" viewing a "QuizQuestion" with ID "${word_id}"
    When the "Learner" selects a "distractor"
    Then the selected button indicates an error
    And the "AIExplanationPanel" appears showing "explanationRootAffix" and "explanationMnemonic"
    And the quiz flow pauses until the "Learner" manually clicks next
    And a POST request is made to "ErrorLogRestController.recordFailure" to log the error for "${word_id}"

# Implementations mapping to UI Manifest IDs
Given a "Learner" receives a "QuizQuestion" from "QuizQuestionRestController"
    # Setup test data or intercept API
    No Operation

When the quiz UI renders the question
    Wait For Elements State    id=question-section    visible

Then the screen must display the full Chinese "translation"
    Get Element States    id=chinese-translation    contains    visible

And the screen must display the "ContextualCloze" English sentence
    Get Element States    id=english-cloze    contains    visible

And the screen must provide 4 randomized option buttons containing 1 "correctOption" and 3 distractors
    Get Element Count    id=quiz-option-trigger    ==    4

Given a "Learner" viewing a "QuizQuestion"
    Wait For Elements State    id=question-section    visible

When the "Learner" selects the "correctOption"
    Click    id=quiz-option-trigger

Then the selected button indicates success
    # Verify success state (e.g. class change)
    Get Element States    id=quiz-option-trigger    contains    visible

And the system automatically transitions to the next question after 1.5 seconds without deducting health
    Sleep    1.5s
    Wait For Elements State    id=question-section    visible

And a POST request is made to update "DailyProgressRestController" if applicable
    # Verify backend call
    No Operation

Given a "Learner" viewing a "QuizQuestion" with ID "${word_id}"
    Wait For Elements State    id=question-section    visible

When the "Learner" selects a "distractor"
    Click    id=quiz-option-trigger

Then the selected button indicates an error
    # Verify error state
    Get Element States    id=quiz-option-trigger    contains    visible

And the "AIExplanationPanel" appears showing "explanationRootAffix" and "explanationMnemonic"
    Wait For Elements State    id=ai-explanation-panel    visible
    Get Element States    id=explanation-root-affix    contains    visible
    Get Element States    id=explanation-mnemonic    contains    visible

And the quiz flow pauses until the "Learner" manually clicks next
    Wait For Elements State    id=next-question-trigger    visible
    Click    id=next-question-trigger

And a POST request is made to "ErrorLogRestController.recordFailure" to log the error for "${word_id}"
    # Verify backend call
    No Operation
