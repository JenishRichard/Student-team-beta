Feature: Room Service API Tests

Background:
    * def token = 'eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhMDAzMzYxNDRAc3R1ZGVudC50dXMuaWUiLCJyb2xlcyI6WyJBRE1JTiJdLCJpYXQiOjE3NzUwNTQ3MTUsImV4cCI6MTc3NTA1ODMxNX0.pRlujXikRoEXChvR8MMC2M0fXDsANaYio0POpF_dSZs'
    * header Authorization = 'Bearer ' + token

Scenario: Get all rooms
    Given url roomServiceUrl + '/rooms'
    When method GET
    Then status 200
    And match response != null

Scenario: Create room
    Given url roomServiceUrl + '/rooms'
    And request
    """
    {
      "roomNumber": "Z101",
      "building": "Engineering and Science",
      "capacity": 40,
      "type": "LAB",
      "available": true
    }
    """
    When method POST
    Then status 201
    And match response.roomNumber == "Z101"

Scenario: Get room by id
    Given url roomServiceUrl + '/rooms/2'
    When method GET
    Then status 200
    And match response.id == 2

Scenario: Update room
    Given url roomServiceUrl + '/rooms/2'
    And request
    """
    {
      "roomNumber": "X101",
      "building": "Engineering and Science",
      "capacity": 30,
      "type": "CLASSROOM",
      "available": false
    }
    """
    When method PUT
    Then status 200
    And match response.capacity == 30

Scenario: Delete room
    Given url roomServiceUrl + '/rooms/2'
    When method DELETE
    Then status 204