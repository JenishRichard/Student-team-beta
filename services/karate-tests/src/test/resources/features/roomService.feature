Feature: Room Service API Tests

Background:
    Given url authServiceUrl + '/auth/login'
    And request
    """
    {
      "email": "admin@tus.ie",
      "password": "Admin@123"
    }
    """
    When method POST
    Then status 200
    * def token = response.accessToken

     * def createRoom =
    """
    function(){
        var result = karate.call({
            url: roomServiceUrl + '/rooms',
            method: 'POST',
            request: {
              roomNumber: 'Z102',
              building: 'Engineering and Science',
              capacity: 40,
              type: 'LAB',
              available: true
            }
        });
        return result.response.id;
    }
    """
    * def roomId = callonce createRoom

Scenario: Get all rooms
    Given url roomServiceUrl + '/rooms'
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response != null

Scenario: Get room by id
    Given url roomServiceUrl + '/rooms/3'
    And header Authorization = 'Bearer ' + token
    When method GET
    Then status 200
    And match response.id == 3  

Scenario: Update room
    Given url roomServiceUrl + '/rooms/'  + roomId
    And header Authorization = 'Bearer ' + token
    And request
    """
    {
      "roomNumber": "Z102",
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
    Given url roomServiceUrl + '/rooms/' + roomId
    And header Authorization = 'Bearer ' + token
    When method DELETE
    Then status 204