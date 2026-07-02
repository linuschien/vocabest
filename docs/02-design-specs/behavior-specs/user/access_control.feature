Feature: Access Control and Admin Management
  As a System
  I want to enforce Role-Based Access Control (RBAC)
  So that Learner privacy is protected and Core Data is secure

  Scenario Outline: Learner Restricted to Own Data
    Given a "Learner" authenticated with ID "<learner_id>"
    When a request is made to access data belonging to "<target_user_id>" via the "DailyProgressRestControllerAdapter"
    Then the external promise is fulfilled with a "<expected_status>" response

    Examples:
      | learner_id | target_user_id | expected_status |
      | ${USER_1}  | ${USER_1}      | 200 OK          |
      | ${USER_1}  | ${USER_2}      | 403 Forbidden   |

  Scenario Outline: Admin Access to Global User Data (GraphQL)
    Given an "Admin" authenticated user
    When the "Admin" requests a global user list via the "UserGraphQLResolverAdapter" ("listUsers")
    Then the external promise is fulfilled with a "200 OK" response

    Examples:
      | admin_id   | query  | expected_status |
      | ${ADMIN_1} | "{}"   | 200 OK          |

  Scenario Outline: Learner Forbidden from Admin Ports
    Given a "Learner" authenticated user
    When the "Learner" attempts to create a new "WordBank" via the "WordBankRestControllerAdapter"
    Then the external promise is fulfilled with a "403 Forbidden" response

    Examples:
      | learner_id | action   |
      | ${USER_1}  | CREATE   |
