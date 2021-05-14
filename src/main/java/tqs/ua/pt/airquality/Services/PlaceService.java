package tqs.ua.pt.airquality.Services;

import javassist.NotFoundException;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tqs.ua.pt.airquality.ApiConnection;
import tqs.ua.pt.airquality.Cache;
import tqs.ua.pt.airquality.Entities.*;
import java.net.URISyntaxException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.*;

@Service
@Transactional
public class PlaceService {
    private static final long TIME_TO_LIVE = 120;
    private static final long TIMER = 60;
    private final Cache<String, Place> cache = new Cache<>(TIME_TO_LIVE, TIMER);

    public Place queryMainAPI(String url, Place queryObj, boolean byName) {       //pode retornar air info, pollen info,air history
        ApiConnection api = new ApiConnection();
        if (!api.isMainAPIAvailable())
            return null;
        ResponseEntity<Map> res = api.connectMainApi(url);

        try {
            if (res.getStatusCode().is2xxSuccessful()) {
                if (queryObj != null) { //chamada ao pollen
                    ArrayList<Object> x = (ArrayList<Object>) res.getBody().get("data");

                    LinkedHashMap<String, Object> info = (LinkedHashMap<String, Object>) x.get(0);

                    LinkedHashMap<String, Object> count = (LinkedHashMap<String, Object>) info.get("Count");
                    LinkedHashMap<String, Object> risk = (LinkedHashMap<String, Object>) info.get("Risk");

                    queryObj.getPollen_count().put("Grass", Integer.parseInt(count.get("grass_pollen").toString()));
                    queryObj.getPollen_count().put("Tree", Integer.parseInt(count.get("tree_pollen").toString()));
                    queryObj.getPollen_count().put("Weed", Integer.parseInt(count.get("weed_pollen").toString()));

                    queryObj.getPollen_risk().put("Grass", risk.get("grass_pollen").toString());
                    queryObj.getPollen_risk().put("Tree", risk.get("tree_pollen").toString());
                    queryObj.getPollen_risk().put("Weed", risk.get("weed_pollen").toString());

                    return queryObj;
                }
                else { //criar objeto base
                    ArrayList<Object> x = (ArrayList<Object>) res.getBody().get("stations");

                    LinkedHashMap<String, Object> info = (LinkedHashMap<String, Object>) x.get(0);

                    Place p = new Place(info.get("city").toString(), Double.parseDouble(info.get("lat").toString()), Double.parseDouble(info.get("lng").toString()));

                    String isoDate = (info.get("updatedAt")).toString();

                    Date date;
                    if(byName) {
                        // getByName
                        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-mm-dd HH:MM:ss");
                        date = formatter.parse(isoDate);
                        p.setUpdateDate(date);
                    }
                    else{
                        // getByCoords
                        TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(isoDate);
                        Instant i = Instant.from(ta);
                        date = Date.from(i);
                    }
                    p.setUpdateDate(date);

                    // Elements info
                    Map<String, Double> dict = p.getAirElements();
                    dict.put("CO", Double.parseDouble(info.get("CO").toString()));
                    dict.put("NO2", Double.parseDouble(info.get("NO2").toString()));
                    dict.put("OZONE", Double.parseDouble(info.get("OZONE").toString()));
                    dict.put("PM10", Double.parseDouble(info.get("PM10").toString()));
                    dict.put("PM25", Double.parseDouble(info.get("PM25").toString()));
                    dict.put("SO2", Double.parseDouble(info.get("SO2").toString()));

                    p.setAirElements(dict);
                    LinkedHashMap<String, Object> aqiDict = (LinkedHashMap<String, Object>) info.get("aqiInfo");

                    //AQI Info
                    List<String> list = p.getAirQualityInfo();
                    list.add(info.get("AQI").toString());
                    list.add(aqiDict.get("pollutant").toString());
                    list.add(aqiDict.get("concentration").toString());
                    list.add(aqiDict.get("category").toString());
                    p.setAirQualityInfo(list);
                    return p;
                }
            }else
                return null;
        }
        catch (Exception e) {
            return null;
        }
    }

    public Place query2ndAPI(String url,String name){      //vai Air INFO, NNC pollen
        ApiConnection api = new ApiConnection();
        Map<String,Object> res = api.connect2ndApi(url);

        try {
            if (res.get("status").equals("ok")){
                LinkedHashMap<String, Object> info = (LinkedHashMap<String, Object>) res.get("data");

                Object city = ((LinkedHashMap<String, Object>) info.get("city")).get("name");
                ArrayList<Double> geo = (ArrayList<Double>) ((LinkedHashMap<String, Object>) info.get("city")).get("geo");

                Place place;
                if (name == null)
                    place = new Place(city.toString(), geo.get(0), geo.get(1));
                else
                    place = new Place(name, geo.get(0), geo.get(1));
                place.getAirQualityInfo().add(info.get("aqi").toString());
                place.getAirQualityInfo().add(info.get("dominentpol").toString());

                // Elements info
                LinkedHashMap<String, Object> elements = (LinkedHashMap<String, Object>) info.get("iaqi");
                place.getAirQualityInfo().add(((LinkedHashMap<String, Object>) elements.get(info.get("dominentpol").toString())).get("v").toString());
                for (Map.Entry<String, Object> entry : elements.entrySet()) {
                    place.getAirElements().put(entry.getKey(), Double.parseDouble(((LinkedHashMap<String, Object>) entry.getValue()).get("v").toString()));
                }

                // Update Date
                String isoDate = ((LinkedHashMap<String, Object>) info.get("time")).get("iso").toString();
                TemporalAccessor ta = DateTimeFormatter.ISO_INSTANT.parse(isoDate);
                Instant i = Instant.from(ta);
                Date d = Date.from(i);
                place.setUpdateDate(d);

                return place;
            }
            return null;
        }
        catch (Exception e) {
            return null;
        }
    }

    /*
    public Map<String, Place> getPollutionStatistics() throws NotFoundException, URISyntaxException {
        Map<String,Place> places = new HashMap<>();

        if (cache.get("most") == null) {
            Place most = queryMainAPI("https://api.ambeedata.com/latest/by-order/worst", null,false);
            if (most != null){
                cache.put("most", most);
                places.put("worst",most);
            }
        }
        if (cache.get("least") == null) {
            Place least = queryMainAPI("https://api.ambeedata.com/latest/by-order/best", null,false);
            if (least != null){
                cache.put("least", least);
                places.put("best",least);
            }
        }
        if (places.size() == 0)
            throw new NotFoundException("It was not possible to connect to this API");
        return places;
    }*/

    public Place getPlaceWithName(String name) throws URISyntaxException{
        Place place = cache.get(name.toLowerCase());

        if(place == null) {
            place = queryMainAPI("https://api.ambeedata.com/latest/by-city?city=" + name, null,true);

            if(place != null) {
                place = queryMainAPI("https://api.ambeedata.com/latest/pollen/by-place?place=" + name,place, true);
                cache.put(place.getName().toLowerCase(), place);
            } else {
                place = query2ndAPI("https://api.waqi.info/feed/" + name, name);
                if(place != null)
                    cache.put(place.getName().toLowerCase(), place);
            }
        }
        return place;
    }

    public Place getPlaceByCoords(String lat, String lng) throws URISyntaxException {
        double latitude;
        double longitude;
        try{
            latitude = Double.parseDouble(lat);
            longitude = Double.parseDouble(lng);
        }catch (Exception e){
            throw new URISyntaxException("URL","Invalid latitude or longitude value");
        }
        Place p = cache.findInCacheObjs(latitude,longitude);

        if(p == null) {
            p = queryMainAPI("https://api.ambeedata.com/latest/by-lat-lng?lat=" + latitude + "&lng=" + longitude, null, false);
            if (p != null) {
                p = queryMainAPI("https://api.ambeedata.com/latest/pollen/by-lat-lng?lat=" + latitude + "&lng=" + longitude, p, false);
                cache.put(p.getName().toLowerCase(), p);
            } else {
                p = query2ndAPI("https://api.waqi.info/feed/geo:" + latitude + ";" + longitude, null);
                if (p != null)
                    cache.put(p.getName().toLowerCase(), p);
            }
        }
        return p;
    }

    public Place getHere() throws URISyntaxException{
        Place p = cache.get("here");
        if(p == null) {
            p = query2ndAPI("https://api.waqi.info/feed/here", null);
            if(p != null){
                cache.put("here", p);
            }
        }
        return p;
    }

    public Map<String, Object> getCacheInfo() {
        Map<String, Object> info = new HashMap<>();

        info.put("hits",cache.getHits());
        info.put("TTL",cache.getTimeToLive());
        info.put("misses",cache.getMisses());
        info.put("requests",cache.getRequests());
        info.put("LastRefresh",DateFormat.getDateTimeInstance().format(cache.getLastRefresh()));
        info.put("Cache Total Objects",cache.getMyCacheObjs().size());
        return info;
    }
}
