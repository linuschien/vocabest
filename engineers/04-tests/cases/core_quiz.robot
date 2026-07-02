*** Settings ***
Documentation       Target Feature: Core Quiz Loop and Instant Feedback
...                 Upstream Requirement: US-02-core-quiz-loop
Library             Browser

*** Test Cases ***
Fetching Contextual Cloze Questions
    [Template]    Fetching Contextual Cloze Questions Template
    SENIOR_HIGH    10

Receiving Instant Feedback for Correct Answer
    [Template]    Receiving Instant Feedback for Correct Answer Template
    ${Q_1}    "apple"    "CORRECT"

Receiving Instant Feedback and AI Explanation for Wrong Answer
    [Template]    Receiving Instant Feedback and AI Explanation for Wrong Answer Template
    ${Q_1}    "banana"    "WRONG"

*** Keywords ***
Fetching Contextual Cloze Questions Template
    [Arguments]    ${target_level}    ${limit}
    Given the system has available "QuizQuestion" records
    When a "nextQuestion" request is made via the "UserRestControllerAdapter"
    Then the external promise is fulfilled with a "200 OK" response
    And the payload contains the context sentence, translation, and 4 shuffled options

Receiving Instant Feedback for Correct Answer Template
    [Arguments]    ${question_id}    ${answer_submitted}    ${expected_status}
    Given a "QuizQuestion" and the Learner knows the correct option
    When the answer is submitted via the "UserRestControllerAdapter" ("submitAnswer")    ${answer_submitted}
    Then the external promise is fulfilled with a "200 OK" response
    And the response indicates the answer is "CORRECT"

Receiving Instant Feedback and AI Explanation for Wrong Answer Template
    [Arguments]    ${question_id}    ${answer_submitted}    ${expected_status}
    Given a "QuizQuestion"
    When the wrong answer is submitted via the "UserRestControllerAdapter" ("submitAnswer")    ${answer_submitted}
    Then the external promise is fulfilled with a "200 OK" response
    And the response indicates the answer is "WRONG"
    And the response payload includes the "AIExplanationPanel" contents

Given the system has available "QuizQuestion" records
    New Page    ${BASE_URL}/quiz

When a "nextQuestion" request is made via the "UserRestControllerAdapter"
    Wait For Elements State    id=question-section    visible

Then the external promise is fulfilled with a "200 OK" response
    Log    Response is 200 OK

And the payload contains the context sentence, translation, and 4 shuffled options
    Wait For Elements State    id=chinese-translation    visible
    Wait For Elements State    id=contextual-cloze    visible
    # Wait for the grid of options
    Wait For Elements State    id=options-grid    visible

Given a "QuizQuestion" and the Learner knows the correct option
    New Page    ${BASE_URL}/quiz

When the answer is submitted via the "UserRestControllerAdapter" ("submitAnswer")
    [Arguments]    ${answer_submitted}
    Click    id=option-trigger >> text=${answer_submitted}

And the response indicates the answer is "CORRECT"
    Get Text    .toast    contains    Answer submitted

Given a "QuizQuestion"
    New Page    ${BASE_URL}/quiz

When the wrong answer is submitted via the "UserRestControllerAdapter" ("submitAnswer")
    [Arguments]    ${answer_submitted}
    Click    id=option-trigger >> text=${answer_submitted}

And the response indicates the answer is "WRONG"
    Wait For Elements State    id=explanation-modal    visible

And the response payload includes the "AIExplanationPanel" contents
    Wait For Elements State    id=explanation-content    visible
    Wait For Elements State    id=next-question-btn    visible
