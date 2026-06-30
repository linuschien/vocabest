Feature: Admin Data Pipeline and Batch Operations
  As an Admin or System Component
  I want to trigger bulk data imports and batch generation via API
  So that the system is hydrated with normalized vocabulary words and quiz questions

  Scenario Outline: Official Vocabulary Import and Normalization
    Given a structured payload containing a "file" (MD/CSV/JSON upload)
    When the "Admin" makes a POST request to "VocabularyWordRestController.importBulk" with the payload
    Then the API must process the source file and return an "OperationStatus" indicating success
    And the system must insert the data into the "VocabularyWordRepository"
    And the system must apply deduplication rules for identical words across target levels
    And the system must tag each word with the appropriate "level"

    Examples:
      | file             | expected_status |
      | jce_2000.csv     | success         |
      | gsat_7000.md     | success         |

  Scenario Outline: AI Batch Generation of Quiz Questions
    Given the "VocabularyWordRepository" contains seeded words
    When a "System" or "Admin" makes a POST request to "QuizQuestionRestController.generateBatch" with "maxWordsToProcess" and optional "targetLevel"
    Then the API must return an "OperationStatus" indicating success
    And the background process must generate a "ContextualCloze", "translation", and grammatically consistent distractors
    And the generation must respect the historical ExamFrequency metadata
    And the valid formats must be persisted to the "QuizQuestionRepository"

    Examples:
      | maxWordsToProcess | targetLevel       | expected_status |
      | 50                | JUNIOR_BASIC_1200 | success         |
      | 200               |                   | success         |
