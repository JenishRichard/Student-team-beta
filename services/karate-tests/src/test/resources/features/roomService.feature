Feature: Room Service API Tests

Background:
    * def authResult = callonce read('classpath:features/helpers/auth.feature')
    * def token = authResult.token

    * def roomResult = callonce read('classpath:features/helpers/createRoom.feature')
    * def roomId = roomResult.roomId
    * def roomNumber = roomResult.roomNumber
    
Scenario: Get all rooms
    Given url roomServiceUrl + '/rooms'
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response != null

Scenario: Get room by id
    Given url roomServiceUrl + '/rooms/' + roomId
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response.id == roomId
    And match response.roomNumber == roomNumber

Scenario: Update room
    Given url roomServiceUrl + '/rooms/' + roomId
    And header Authorization = 'Bearer ' + token
    And request
    """
    {
      "roomNumber": "#(roomNumber)",
      "building": "Engineering Block Updated",
      "capacity": 50,
      "type": "LAB",
      "available": false
    }
    """
    When method PUT
    Then status 200
    And match response.id == roomId
    And match response.building == "Engineering Block Updated"
    And match response.capacity == 50
    And match response.type == "LAB"
    And match response.available == false    

Scenario: Delete room
    Given url roomServiceUrl + '/rooms/' + roomId
    And header Authorization = 'Bearer ' + token
    When method DELETE
    Then status 204

Scenario: Get room with invalid id
    Given url roomServiceUrl + '/rooms/1'
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 404

Scenario: Delete room with invalid id
    Given url roomServiceUrl + '/rooms/1'
    And header Authorization = 'Bearer ' + token
    When method DELETE
    Then status 404

Scenario: Access rooms without token
    Given url roomServiceUrl + '/rooms'
    When method GET
    Then status 401

Scenario: Access rooms with invalid token
    Given url roomServiceUrl + '/rooms'
    And header Authorization = 'Bearer invalid-token'
    When method GET
    Then status 401

Scenario: Create room with missing required field
    Given url roomServiceUrl + '/rooms'
    And header Authorization = 'Bearer ' + token
    And request
    """
    {
      "building": "Engineering and Science",
      "capacity": 50,
      "type": "CLASSROOM",
      "available": 1
    }
    """
    When method POST
    Then status 500

Scenario: Create room with existing room number

    Given url roomServiceUrl + '/rooms'
    And header Authorization = 'Bearer ' + token
    And request
    """
    {
      "roomNumber": "X102",
      "building": "Engineering and Science",
      "capacity": 50,
      "type": "SEMINAR",
      "available": 1
    }
    """
    When method POST
    Then status 500