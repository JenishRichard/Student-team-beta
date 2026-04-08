Feature: Create room for reuse

Background:
    * def authResult = callonce read('classpath:features/helpers/auth.feature')
    * def token = authResult.token

Scenario: Create room

    * def uniqueRoomNumber = 'TEST' + java.lang.System.currentTimeMillis()

    Given url roomServiceUrl + '/rooms'
    And header Authorization = 'Bearer ' + token
    And request
    """
    {
      "roomNumber": "#(uniqueRoomNumber)",
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