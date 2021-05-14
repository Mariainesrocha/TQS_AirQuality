package tqs.ua.pt.airquality.PlaceITtest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.web.server.LocalServerPort;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import tqs.ua.pt.airquality.ApiConnection;
import tqs.ua.pt.airquality.Entities.Place;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PlaceIntegrationTest {

    @LocalServerPort
    int randomServerPort;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private ApiConnection apiConnection;

    @Test
    public void getPlaceByNameTestIT() {
        ResponseEntity<Place> response = restTemplate.exchange("/places/Lisboa", HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotEquals(null, response.getBody());
        assertEquals(Place.class, response.getBody().getClass());

        assertEquals("Lisboa", response.getBody().getName());
        assertNotEquals(null, response.getBody().getLongitude());
        if(apiConnection.isMainAPIAvailable())
            assertEquals(38.7167, response.getBody().getLatitude());
        else
            assertEquals(38.748611111111, response.getBody().getLatitude());
        assertNotEquals(null, response.getBody().getUpdateDate());
    }

    @Test
    public void getPlaceByWrongNameTestIT(){
        ResponseEntity<String> response = restTemplate.exchange("/places/r4ndom", HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNotEquals(null, response.getBody());
        assertEquals(String.class, response.getBody().getClass());
        assertEquals("No places found to this search", response.getBody());
    }

    @Test
    public void getHereTestIT(){
        ResponseEntity<Place> response = restTemplate.exchange("/here", HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotEquals(null, response.getBody());
        assertEquals(Place.class, response.getBody().getClass());
    }

    @Test
    public void getPlaceByCoordsTestIT() {
        ResponseEntity<Place> response = restTemplate.exchange("/places?lat=38.741&long=-9.14", HttpMethod.GET, null, new ParameterizedTypeReference<>() {});
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertNotEquals(null, response.getBody());
        assertEquals(Place.class, response.getBody().getClass());
        if(apiConnection.isMainAPIAvailable()){
            assertEquals(38.7167, response.getBody().getLatitude());
            assertEquals("Lisboa", response.getBody().getName());
        }
        else{
            assertEquals(38.748611111111, response.getBody().getLatitude());
            assertEquals("Entrecampos, Lisboa, Portugal", response.getBody().getName());
        }
        assertNotEquals(null, response.getBody().getUpdateDate());
        assertNotEquals(null, response.getBody().getAirElements());
        assertNotEquals(null, response.getBody().getAirQualityInfo());
    }

    @Test
    public void getPlaceByWrongCoordsTestIT(){
        ResponseEntity<String> response = restTemplate.exchange("/places?lat=11a.1&long=-12.573", HttpMethod.GET, null, String.class);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertNotEquals(null, response.getBody());
        assertEquals(String.class, response.getBody().getClass());
        assertEquals("Error with URL", response.getBody());
    }
}