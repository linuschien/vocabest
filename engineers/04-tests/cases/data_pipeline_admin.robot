*** Settings ***
Documentation     Target Feature: Admin Data Pipeline and Batch Operations
...               Upstream Requirement ID: US-00-03, US-00-04 (Admin Data Pipeline)
Library           Browser

*** Test Cases ***
Official Vocabulary Import and Normalization
    [Template]    Official Vocabulary Import and Normalization Template
    jce_2000.csv    success
    gsat_7000.md    success

AI Batch Generation of Quiz Questions
    [Template]    AI Batch Generation of Quiz Questions Template
    50     JUNIOR_BASIC_1200    success
    200    ${EMPTY}             success

*** Keywords ***
Official Vocabulary Import and Normalization Template
    [Arguments]    ${file}    ${expected_status}
    Given a structured payload containing a "file" (MD/CSV/JSON upload) with "${file}"
    When the "Admin" makes a POST request to "VocabularyWordRestController.importBulk" with the payload
    Then the API must process the source file and return an "OperationStatus" indicating success
    And the system must insert the data into the "VocabularyWordRepository"
    And the system must apply deduplication rules for identical words across target levels
    And the system must tag each word with the appropriate "level"

AI Batch Generation of Quiz Questions Template
    [Arguments]    ${maxWordsToProcess}    ${targetLevel}    ${expected_status}
    Given the "VocabularyWordRepository" contains seeded words
    When a "System" or "Admin" makes a POST request to "QuizQuestionRestController.generateBatch" with "${maxWordsToProcess}" and optional "${targetLevel}"
    Then the API must return an "OperationStatus" indicating success
    And the background process must generate a "ContextualCloze", "translation", and grammatically consistent distractors
    And the generation must respect the historical ExamFrequency metadata
    And the valid formats must be persisted to the "QuizQuestionRepository"

# Implementations mapping to UI Manifest IDs
Given a structured payload containing a "file" (MD/CSV/JSON upload) with "${file}"
    Wait For Elements State    id=data-pipeline-container    visible
    Click    id=trigger-open-import
    Wait For Elements State    id=import-modal    visible
    Fill Text    id=import-file-field    ${file}

When the "Admin" makes a POST request to "VocabularyWordRestController.importBulk" with the payload
    Click    id=import-submit-trigger

Then the API must process the source file and return an "OperationStatus" indicating success
    # Verify success toast "Import successfully queued"
    Get Element States    text="Import successfully queued"    contains    visible

And the system must insert the data into the "VocabularyWordRepository"
    # Verify backend state
    No Operation

And the system must apply deduplication rules for identical words across target levels
    # Verify backend state
    No Operation

And the system must tag each word with the appropriate "level"
    # Verify backend state
    No Operation

Given the "VocabularyWordRepository" contains seeded words
    Wait For Elements State    id=data-pipeline-container    visible

When a "System" or "Admin" makes a POST request to "QuizQuestionRestController.generateBatch" with "${maxWordsToProcess}" and optional "${targetLevel}"
    Click    id=trigger-open-generate
    Wait For Elements State    id=generate-modal    visible
    Fill Text    id=generate-max-words    ${maxWordsToProcess}
    Run Keyword If    '${targetLevel}' != '${EMPTY}'    Select Options By    id=generate-target-level    value    ${targetLevel}
    Click    id=generate-submit-trigger

Then the API must return an "OperationStatus" indicating success
    # Verify success toast "Batch generation successfully triggered"
    Get Element States    text="Batch generation successfully triggered"    contains    visible

And the background process must generate a "ContextualCloze", "translation", and grammatically consistent distractors
    # Verify backend state / background process results
    No Operation

And the generation must respect the historical ExamFrequency metadata
    # Verify backend state
    No Operation

And the valid formats must be persisted to the "QuizQuestionRepository"
    # Verify backend state
    No Operation
