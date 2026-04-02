Feature: Booking Service API Tests

Scenario: Get all bookings
    Given url bookingServiceUrl + '/bookings'
    When method GET
    Then status 200
    And match response != null

