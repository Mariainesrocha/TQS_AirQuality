package tqs.ua.pt.airquality.ControllersTest;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tqs.ua.pt.airquality.Controllers.PlaceRestController;
import tqs.ua.pt.airquality.Entities.Place;
import tqs.ua.pt.airquality.Services.PlaceService;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import java.net.URISyntaxException;
import java.util.*;

@WebMvcTest(PlaceRestController.class)
class RestControllerTest {
    @Autowired
    private MockMvc testServlet;

    @MockBean
    private PlaceService service;

    private static Place p1;
    private static Place p2;
    private static int counter = 1;

    @BeforeAll
    public static void start() {
        System.out.println(" ------------ Starting Tests... ------------ ");
        p1 = new Place("Aveiro", 40.0, -8.0);
        p1.setAirQualityInfo(Arrays.asList("17","CO2","45.0"));
        p2 = new Place("Madrid", 30.0, -12.0);
        Map<String,Integer> pollen = new HashMap<>();
        pollen.put("weed", 5);
        pollen.put("tree", 13);
        p2.setPollen_count(pollen);
    }

    @BeforeEach
    void setup() {
        System.out.println(" ------------ Test nº"+ counter + " ------------ ");
    }

    @AfterEach
    void tearDown() {
        counter++;
    }

    @AfterAll
    static void end() {
        System.out.println(counter-1 == 6? "Success: Total of 6 tests passed":"Attention: some tests failed.");
    }

    @Test
    public void whenGetPlaceByValidName_thenReturnPlace() throws Exception {
        when(service.getPlaceWithName("Aveiro")).thenReturn(p1);

        testServlet.perform(get("/places/Aveiro").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name", is("Aveiro")))
                .andExpect(jsonPath("$.latitude", is(40.0)))
                .andExpect(jsonPath("$.airQualityInfo[1]", is("CO2")))
                .andExpect(jsonPath("$.longitude", is(-8.0)));

        verify(service, times(1)).getPlaceWithName(Mockito.anyString());
    }


    @Test
    public void whenGetPlaceByNonExistentName_thenReturnNull() throws Exception {
        when(service.getPlaceWithName("Batatas")).thenReturn(null);

        testServlet.perform(get("/places/Batatas").contentType(MediaType.TEXT_PLAIN_VALUE))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(content().string("No places found to this search"));

        verify(service, times(1)).getPlaceWithName("Batatas");
    }

    @Test
    public void whenGetPlaceByValidCoords_thenReturnPlace() throws Exception {
        when(service.getPlaceByCoords("30.0", "-12.0")).thenReturn(p2);

        testServlet.perform(get("/places?lat=30.0&long=-12.0").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Madrid")))
                .andExpect(jsonPath("$.latitude", is(30.0)))
                .andExpect(jsonPath("$.pollen_count", hasEntry("tree",13)));

        verify(service, times(1)).getPlaceByCoords(any(), any());
    }

    @Test
    public void whenGetPlaceByInvalidCoords_thenReturnPlace() throws Exception {
        when(service.getPlaceByCoords("1000000.30", "-123333.0")).thenReturn(null); //scenario 1
        when(service.getPlaceByCoords("aa", "-123333.0")).thenThrow(new URISyntaxException("URL","Invalid latitude or longitude value"));

        //scenario 1
        testServlet.perform(get("/places?lat=1000000.30&long=-123333.0").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isNotFound());

        //scenario 2
        testServlet.perform(get("/places?lat=aa&long=-123333.0").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isBadRequest());
        verify(service, times(2)).getPlaceByCoords(any(), any());
    }

    @Test
    public void whenGetPlaceHere_thenReturnPlace() throws Exception {
        when(service.getHere()).thenReturn(p1);

        testServlet.perform(get("/here").contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().is2xxSuccessful())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.name", is("Aveiro")))
                .andExpect(jsonPath("$.longitude", is(-8.0)));

        verify(service, times(1)).getHere();
    }

    @Test
    public void whenGetCacheInfo_thenReturnCacheInfo() throws Exception {
        Map<String, Object> cacheDetails = new HashMap<>();
        cacheDetails.put("requests", 3);
        cacheDetails.put("hits", 1);
        cacheDetails.put("misses",2 );
        cacheDetails.put("TTL", 60);
        cacheDetails.put("LastRefresh", System.currentTimeMillis());

        when(service.getCacheInfo()).thenReturn(cacheDetails);

        testServlet.perform(get("/cache").contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().is2xxSuccessful())
            .andExpect(jsonPath("$.hits", is(1)))
            .andExpect(jsonPath("$.misses", is(2)))
            .andExpect(jsonPath("$.requests", is(3)))
            .andExpect(jsonPath("$.TTL", is(60)));

        verify(service, times(1)).getCacheInfo();
    }
}