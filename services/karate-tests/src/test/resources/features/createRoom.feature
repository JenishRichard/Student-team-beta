Feature: Create room for reuse

Scenario: Create room

    Given url roomServiceUrl + '/rooms'
    And header Authorization = 'Bearer ' + token
    And request
    """
    {
      "roomNumber": "TEST1",
      "building": "Engineering Block",
      "capacity": 40,
      "type": "CLASSROOM",
      "available": true
    }
    """
    When method POST
    Then status 201
    * def roomId = response.id
    * def roomNumber = response.roomNumber