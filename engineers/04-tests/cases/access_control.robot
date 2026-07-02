*** Settings ***
Documentation       Target Feature: Access Control and Admin Management
...                 Upstream Requirement: US-05-access-control-and-admin
Library             Browser

*** Test Cases ***
Learner Restricted to Own Data
    [Template]    Learner Restricted to Own Data Template
    ${USER_1}    ${USER_1}    200 OK
    ${USER_1}    ${USER_2}    403 Forbidden

Admin Access to Global User Data (GraphQL)
    [Template]    Admin Access to Global User Data (GraphQL) Template
    ${ADMIN_1}    "{}"    200 OK

Learner Forbidden from Admin Ports
    [Template]    Learner Forbidden from Admin Ports Template
    ${USER_1}    CREATE

*** Keywords ***
Learner Restricted to Own Data Template
    [Arguments]    ${learner_id}    ${target_user_id}    ${expected_status}
    Given a "Learner" authenticated with ID "${learner_id}"
    When a request is made to access data belonging to "${target_user_id}" via the "DailyProgressRestControllerAdapter"
    Then the external promise is fulfilled with a "${expected_status}" response

Admin Access to Global User Data (GraphQL) Template
    [Arguments]    ${admin_id}    ${query}    ${expected_status}
    Given an "Admin" authenticated user
    When the "Admin" requests a global user list via the "UserGraphQLResolverAdapter" ("listUsers")
    Then the external promise is fulfilled with a "${expected_status}" response

Learner Forbidden from Admin Ports Template
    [Arguments]    ${learner_id}    ${action}
    Given a "Learner" authenticated user
    When the "Learner" attempts to create a new "WordBank" via the "WordBankRestControllerAdapter"
    Then the external promise is fulfilled with a "403 Forbidden" response

Given a "Learner" authenticated with ID "${learner_id}"
    New Page    ${BASE_URL}/login
    Log    Authenticated as ${learner_id}

When a request is made to access data belonging to "${target_user_id}" via the "DailyProgressRestControllerAdapter"
    New Page    ${BASE_URL}/dashboard?userId=${target_user_id}

Then the external promise is fulfilled with a "${expected_status}" response
    Log    Expected status: ${expected_status}

Given an "Admin" authenticated user
    New Page    ${BASE_URL}/login

When the "Admin" requests a global user list via the "UserGraphQLResolverAdapter" ("listUsers")
    New Page    ${BASE_URL}/admin
    Wait For Elements State    id=user-table    visible

Given a "Learner" authenticated user
    New Page    ${BASE_URL}/login

When the "Learner" attempts to create a new "WordBank" via the "WordBankRestControllerAdapter"
    New Page    ${BASE_URL}/dictionary
    Wait For Elements State    id=create-wordbank-btn    visible
    Click    id=create-wordbank-btn
    Wait For Elements State    id=submit-wordbank-btn    visible
    Click    id=submit-wordbank-btn
