Feature: Room and Booking integration tests

  Scenario: Services are reachable
    Given url roomServiceUrl + '/rooms'
    And header Authorization = 'Bearer ' + 'YOUR_TOKEN_HERE'
    When method GET
    Then status 200

    Given url bookingServiceUrl + '/bookings'
    And header Authorization = 'Bearer ' + 'YOUR_TOKEN_HERE'
    When method GET
    Then status 200