Feature: Booking Service API Tests

Background:
    * def authResult = callonce read('classpath:features/helpers/auth.feature')
    * def token = authResult.token

    * def bookingResult = callonce read('classpath:features/helpers/createBooking.feature')
    * def bookingId = bookingResult.bookingId
    * def bookedBy = bookingResult.bookedBy

Scenario: Get all bookings
    Given url bookingServiceUrl + '/bookings'
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response != null

Scenario: Get booking by id
    Given url bookingServiceUrl + '/bookings/' + bookingId
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response.id == bookingId
    And match response.bookedBy == bookedBy

Scenario: Cancel booking
    Given url bookingServiceUrl + '/bookings/' + bookingId + '/cancel'
    And header Authorization = 'Bearer ' + token
    When method PUT
    Then status 200
    And match response.id == bookingId
    And match response.status == "CANCELLED"

 Scenario: Delete booking
    Given url bookingServiceUrl + '/bookings/' + bookingId
    And header Authorization = 'Bearer ' + token
    When method DELETE
    Then status 200

 Scenario: Get booking with invalid id
    Given url bookingServiceUrl + '/bookings/1'
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 400
	
Scenario: Delete booking with invalid id
    Given url bookingServiceUrl + '/bookings/1'
    And header Authorization = 'Bearer ' + token
    When method DELETE
    Then status 400
	
Scenario: Cancel booking with invalid id
    Given url bookingServiceUrl + '/bookings/1/cancel'
    And header Authorization = 'Bearer ' + token
    When method PUT
    Then status 400

Scenario: Access bookings without token
    Given url bookingServiceUrl + '/bookings'
    When method GET
    Then status 401

Scenario: Access bookings with invalid token
    Given url bookingServiceUrl + '/bookings'
    And header Authorization = 'Bearer invalid-token'
    When method GET
    Then status 401

Scenario: Create duplicate booking for same slot

    Given url bookingServiceUrl + '/bookings'
    And header Authorization = 'Bearer ' + token
    And request
    """
    {
		"bookedBy": "teacher@test.com",
		"id": 11,
		"roomId": 18,
		"bookedByIdentity": "TEACHER",
		"bookingDate": "2026-03-10",
		"bookingTime": "10:00-12:00",
		"status": "CONFIRMED"
    }
    """
    When method POST
    Then status 400   