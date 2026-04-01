Feature: Booking Service API Tests

Scenario: Get all bookings
    Given url bookingServiceUrl + '/bookings'
    When method GET
    Then status 200
    And match response != null

Scenario: Create booking
    Given url bookingServiceUrl + '/bookings'
    And request
    """
    {
      "roomId": 18,
      "bookedBy": "teacher@test.com",
      "bookedByIdentity": "TEACHER",
      "bookingDate": "2026-03-10",
      "bookingTime": "10:00-12:00",
      "status": "CONFIRMED"
    }
    """
    When method POST
    Then status 201
    And match response.roomId == 18
    And match response.bookedBy == "teacher@test.com"
    And match response.bookedByIdentity == "TEACHER"
    And match response.bookingDate == "2026-03-10"
    And match response.bookingTime == "10:00-12:00"
    And match response.status == "CONFIRMED"

Scenario: Get booking by id
    Given url bookingServiceUrl + '/bookings/9'
    When method GET
    Then status 200
    And match response.id == 9
    And match response.roomId == 18
    And match response.bookedBy == "teacher@test.com"

Scenario: Update booking
    Given url bookingServiceUrl + '/bookings/9'
    And request
    """
    {
      "roomId": 18,
      "bookedBy": "teacher@test.com",
      "bookedByIdentity": "TEACHER",
      "bookingDate": "2026-03-10",
      "bookingTime": "11:00-13:00",
      "status": "CONFIRMED"
    }
    """
    When method PUT
    Then status 200
    And match response.id == 9
    And match response.bookingTime == "11:00-13:00"

Scenario: Delete booking
    Given url bookingServiceUrl + '/bookings/9'
    When method DELETE
    Then status 204