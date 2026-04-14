Feature: Create booking for reuse

Background:
    * def authResult = callonce read('classpath:features/helpers/auth.feature')
    * def token = authResult.token

Scenario: Create booking

    Given url bookingServiceUrl + '/bookings'
    And header Authorization = 'Bearer ' + token
    And request
    """
    {
      "roomId": 3,
      "bookedBy": "test@tus.ie",
      "bookedByIdentity": "TEACHER",
      "bookingDate": "2026-04-10",
      "bookingTime": "14:00-15:00"
    }
    """
    When method POST
    Then status 201
    * def bookingId = response.id
    * def bookedBy = response.bookedBy
