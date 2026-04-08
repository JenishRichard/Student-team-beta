Feature: Get auth token

Scenario: Login and return token
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