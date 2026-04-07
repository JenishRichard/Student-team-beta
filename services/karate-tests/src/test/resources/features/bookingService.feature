Feature: Booking Service API Tests

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

Scenario: Get all bookings
    Given url bookingServiceUrl + '/bookings'
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response != null
