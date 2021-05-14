package tqs.ua.pt.airquality.Controllers;
import javassist.NotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tqs.ua.pt.airquality.Entities.*;
import tqs.ua.pt.airquality.Services.PlaceService;
import java.net.URISyntaxException;
import java.util.Map;

@RestController
public class PlaceRestController {
    @Autowired
    private PlaceService service;

    @GetMapping(path="/places/{name}")
    public ResponseEntity<Object> getPlaceWithName(@PathVariable(value = "name") String name) {
        try {
            Place place = service.getPlaceWithName(name);
            if (place == null)
                return new ResponseEntity<>("No places found to this search", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(place, HttpStatus.OK);
        }catch (URISyntaxException e) {
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
            return new ResponseEntity<>("Error with URL", HttpStatus.BAD_REQUEST);
        }
    }

    /*
    @GetMapping(path="/places/pollutionStats")
    public ResponseEntity<Object> getPollutionStatistics() {
        try {
            Map<String, Place> places = service.getPollutionStatistics();
            if (places.size() == 2)
                return new ResponseEntity<>(places, HttpStatus.OK);
            else
                return new ResponseEntity<>("Error while getting best and worst polluted places", HttpStatus.NOT_FOUND);
        }catch (NotFoundException e){
            return new ResponseEntity<>("Expired API connection, the number of free requests today its bound", HttpStatus.BANDWIDTH_LIMIT_EXCEEDED);
        } catch (URISyntaxException e) {
            return new ResponseEntity<>("Error with URL", HttpStatus.BAD_REQUEST);
        }
    }*/

    @GetMapping("/here")
    public ResponseEntity<Object> getHere() {
        try {
            Place place = service.getHere();
            if (place == null)
                return new ResponseEntity<>("Current location air quality information not found", HttpStatus.NOT_FOUND);
            return new ResponseEntity<>(place, HttpStatus.OK);
        } catch (URISyntaxException e) {
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
            return new ResponseEntity<>("Error while getting cache info", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
