package tqs.ua.pt.airquality.PlaceITtest;

import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.web.server.LocalServerPort;
import static org.hamcrest.CoreMatchers.*;
import static org.hamcrest.Matchers.hasKey;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class AssuredRestControllerIT {

    @LocalServerPort
    int randomServerPort;


    @Test
    public void whenPlaceByNameExists_thenReturnPlace() {
        RestAssured.given().port(randomServerPort).get("/places/lisboa")
                .then()
                .statusCode(200)
                .and().body("name", containsStringIgnoringCase("lisboa"))
                .and().body("$", hasKey("updateDate"))
                .and().body("$", hasKey("airElements"))
                .and().body("$", hasKey("airQualityInfo"))
                .and().body("$", hasKey("latitude"))
                .and().body("$", hasKey("longitude"));
    }

    @Test
    void whenNonExistentPlace_thenReturnNotFound() {
        RestAssured.given().port(randomServerPort).get("/places/mos12cas")
                .then()
                .statusCode(404)
                .and().body(containsString("No places found to this search"));
    }

    @Test
    void whenPlaceLatAndLong_thenReturnPlace() {
        RestAssured.given().port(randomServerPort).get("/places?lat=40.4&long=-3.7")
                .then()
                .statusCode(200)
                .and().body("$", hasKey("updateDate"))
                .and().body("$", hasKey("airElements"))
                .and().body("$", hasKey("airQualityInfo"))
                .and().body("$", hasKey("name"));
    }
}