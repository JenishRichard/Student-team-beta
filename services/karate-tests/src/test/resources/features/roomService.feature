Feature: Room Service API Tests

Background:
    Given url authServiceUrl + '/auth/login'
    And request
    """
    {
      "email": "admin@tus.ie",
      "password": "Admin@123"
    }
    """
    When method POST
    Then status 200
    * def token = response.accessToken
    

Scenario: Get all rooms
    Given url roomServiceUrl + '/rooms'
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response != null

Scenario: Get room by id
    Given url roomServiceUrl + '/rooms/3'
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response.id == 3