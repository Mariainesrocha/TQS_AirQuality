Feature: Interact with Healthy Breeze Website
  Background: Interface Tests
    Given Open Browser

  Scenario: See error page when invalid page and back to HomePage
    When I navigate to 'http://localhost:8080/random'
    And I click on 'Go To Homepage'
    Then I should be shown results including 'Healthy Breeze'

  Scenario: Search by existent city name
    When I navigate to 'http://localhost:8080/'
    And I write 'Lisboa' in the 'city search bar'
    And I click on 'Search'
    And I scroll to 'Search results' section
    Then I should be shown a table row with results including 'lisboa' in city name

  Scenario: Search by valid city coordinates
    When I navigate to 'http://localhost:8080/'
    And  I write '40.416' in the 'latitude search bar'
    And I write '-3.703' in the 'longitude search bar'
    And I click on 'Go'
    Then I should be shown a table row with results including 'madrid' in city name


  Scenario: Search by invalid city name
    When I navigate to 'http://localhost:8080/'
    And I write 'lisb212n' in the 'city search bar'
    And I click on 'Search'
    Then I should be shown an error message with body like 'Failed to search, try again later! If the error persists maybe we don\'t have information about the chosen city.'

  Scenario: Search by invalid city coordinates
    When I navigate to 'http://localhost:8080/'
    And  I write '40.607' in the 'latitude search bar'
    And I click on 'Go'
    Then I should be shown an error message with body like 'Error with latitude and longitude values'


