package tqs.ua.pt.airquality.Services;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

@Service
@Transactional
public class PlaceService {
    private static final long TIME_TO_LIVE = 120;
    private static final long TIMER = 60;

    private final Cache<String, Place> cache = new Cache<>(TIME_TO_LIVE, TIMER);

    ApiConnection api = new ApiConnection();

    private static Logger log = LogManager.getLogger(PlaceService.class);

    public Place queryMainAPI(String url, Place queryObj, boolean byName) {       //pode retornar air info, pollen info,air history
        if (!api.isMainAPIAvailable())
            return null;
        ResponseEntity<Map> res = api.connectMainApi(url);

        try {
            if (res.getStatusCode().is2xxSuccessful()) {
                log.info("MainApi response code 200");
                if (queryObj != null) { //chamada ao pollen
                    log.info("Filling pollen informations on city "+queryObj.getName());
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

                    log.info("Pollen information filled!");
                    return queryObj;
                }
                else { //criar objeto base
                    log.info("Create place from main api response");

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
            log.debug("Exception:" +e.toString()+"   - caught when retrieving data from main API");
            return null;
        }
    }

    public Place query2ndAPI(String url,String name){      //vai Air INFO, NNC pollen
        Map<String,Object> res = api.connect2ndApi(url);

        try {
            if (res.get("status").equals("ok")){
                log.info("Second api response status is OK");
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
            log.debug("Exception:" +e.toString()+"   - caught when retrieving data from second API");
            return null;
        }
    }

    public Place getPlaceWithName(String name) throws URISyntaxException{
        Place place = cache.get(name.toLowerCase());

        if(place == null) {
            place = queryMainAPI("https://api.ambeedata.com/latest/by-city?city=" + name, null,true);

            if(place != null) {
                log.info("Main API valid results, then query main API for Pollen data");
                place = queryMainAPI("https://api.ambeedata.com/latest/pollen/by-place?place=" + name,place, true);
                cache.put(place.getName().toLowerCase(), place);
            } else {
                log.info("Main API invalid results, then query second API ");
                place = query2ndAPI("https://api.waqi.info/feed/" + name, name);
                if(place != null)
                    cache.put(place.getName().toLowerCase(), place);
            }
        } else
            log.info("Place get name from cache");
        return place;
    }

    public Place getPlaceByCoords(String lat, String lng) throws URISyntaxException {
        double latitude;
        double longitude;
        try{
            latitude = Double.parseDouble(lat);
            longitude = Double.parseDouble(lng);
        }catch (Exception e){
            log.debug("getPlaceByCoords error while trying to parse latitude or longitude value to double");
            throw new URISyntaxException("URL","Invalid latitude or longitude value");
        }
        Place p = cache.findInCacheObjs(latitude,longitude);

        if(p == null) {
            p = queryMainAPI("https://api.ambeedata.com/latest/by-lat-lng?lat=" + latitude + "&lng=" + longitude, null, false);
            if (p != null) {
                log.info("Main API valid results, then query main API for Pollen data");
                p = queryMainAPI("https://api.ambeedata.com/latest/pollen/by-lat-lng?lat=" + latitude + "&lng=" + longitude, p, false);
                cache.put(p.getName().toLowerCase(), p);
            } else {
                log.info("Main API invalid results, then query second API ");
                p = query2ndAPI("https://api.waqi.info/feed/geo:" + latitude + ";" + longitude, null);
                if (p != null)
                    cache.put(p.getName().toLowerCase(), p);
            }
        }else
            log.info("Place get by coords from cache");
        return p;
    }

    public Place getHere() throws URISyntaxException{
        Place p = cache.get("here");
        if(p == null) {
            p = query2ndAPI("https://api.waqi.info/feed/here", null);
            if(p != null){
                log.info("Place returned for current location, 2nd API query");
                cache.put("here", p);
            }
        }else
            log.info("Current Location Place returned from cache");
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
