package tqs.ua.pt.airquality.Controllers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.ua.pt.airquality.Entities.*;
import tqs.ua.pt.airquality.Services.PlaceService;
import java.net.URISyntaxException;
import java.util.Map;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@RestController
public class PlaceRestController {
    @Autowired
    private PlaceService service;

    private static Logger log = LogManager.getLogger(PlaceRestController.class);

    @GetMapping(path="/places/{name}")
    public ResponseEntity<Object> getPlaceWithName(@PathVariable(value = "name") String name) {
        try {
            if(name.matches(".*\\d.*"))
                return new ResponseEntity<>("Invalid name: city name should not contain digits", HttpStatus.BAD_REQUEST);
            Place place = service.getPlaceWithName(name);
            if (place == null)
                return new ResponseEntity<>("No places found to this search", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(place, HttpStatus.OK);
        }catch (URISyntaxException e) {
            log.info("Bad request in getPlaceWithName api method");
            return new ResponseEntity<>("Error with URL", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping(path="/places")
    public ResponseEntity<Object> getPlaceByCoords(@RequestParam(value = "lat") String latitude, @RequestParam(value = "long") String longitude) {
        try {
            Place place = service.getPlaceByCoords(latitude, longitude);
            if (place == null)
                return new ResponseEntity<>("No place found for these coordinates", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(place, HttpStatus.OK);
        } catch (URISyntaxException e){
            log.info("Bad request in getPlaceByCoords api method");
            return new ResponseEntity<>("Error with URL", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/here")
    public ResponseEntity<Object> getHere() {
        try {
            Place place = service.getHere();
            if (place == null)
                return new ResponseEntity<>("Current location air quality information not found", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(place, HttpStatus.OK);
        } catch (URISyntaxException e) {
            log.info("Bad request in getHere api method");
            return new ResponseEntity<>("Error with URL", HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/cache")
    public ResponseEntity<Object> getCacheInfo() {
        try {
            Map<String, Object> data = service.getCacheInfo();
             if (data == null)
                 return new ResponseEntity<>("Error: No cache information found.", HttpStatus.NOT_FOUND);
             return new ResponseEntity<>(data, HttpStatus.OK);
        } catch (Exception e){
            log.info("Exception: "+e.toString()+" in getCacheInfo api method");
            return new ResponseEntity<>("Error while getting cache info", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
