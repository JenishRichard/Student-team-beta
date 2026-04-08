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