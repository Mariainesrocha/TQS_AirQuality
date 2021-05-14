package tqs.ua.pt.airquality;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import java.util.Collections;
import java.util.Map;

@Component
public class ApiConnection {

    private boolean mainAPIAvailable = true;

    private final RestTemplate restTemplate;

    public ApiConnection() {
        restTemplate = new RestTemplate();
    }

    public ResponseEntity<Map> connectMainApi(String url){
        String key = "fdcYhkw3BN7AwZFZ1qU7O4trj272xpar7lgKDfqp";

        var headers = new HttpHeaders();
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        headers.set("x-api-key", key);

        HttpEntity<String> entity = new HttpEntity<>("body", headers);
        ResponseEntity<Map> res;
        try {
           res = this.restTemplate.exchange(url, HttpMethod.GET, entity, Map.class);
        }catch (HttpClientErrorException e){
           if(e.getStatusText().equals("Too Many Requests")){
               System.out.println("Colocar LOG para too many requests");
               this.mainAPIAvailable = false;
           }
           return null;
        }
        return res;
    }

    public Map<String,Object> connect2ndApi(String url) {
        String token = "ba66d05c9ea5c3544d4c22c4db9dd0a2c54f120b";
        return this.restTemplate.getForObject(url +"/?token=" + token, Map.class);
    }

    public boolean isMainAPIAvailable() {
        return this.mainAPIAvailable;
    }
}