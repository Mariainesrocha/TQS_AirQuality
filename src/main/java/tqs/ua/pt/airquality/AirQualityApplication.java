package tqs.ua.pt.airquality;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AirQualityApplication {

    public static void main(String[] args) {
        SpringApplication.run(AirQualityApplication.class, args);
    }

}
// API TO BE CONSULTED
// https://aqicn.org/api/
// token: ba66d05c9ea5c3544d4c22c4db9dd0a2c54f120b
// https://api.waqi.info/search/?token=ba66d05c9ea5c3544d4c22c4db9dd0a2c54f120b&keyword=Braga
// https://aqicn.org/json-api/doc/#api-Map_Queries

// https://api-dashboard.getambee.com/#/signup
// token: eHvVo8K6wl38MzjgH3dk16wR4jKtHqOs4GHgecEq

// USING H2: https://stackabuse.com/integrating-h2-database-with-spring-boot/

// referencias: https://simplemaps.com/data/world-cities