package tqs.ua.pt.airquality.CacheTest;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.function.Executable;
import tqs.ua.pt.airquality.Cache;
import static org.junit.jupiter.api.Assertions.*;

class CacheTest {

    private static Cache<String, Integer> cache;
    private static final long TIME_TO_LIVE = 5;
    private static final long TIMER = 5;
    private static int counter = 1;

    @BeforeEach
    void setup() {
        System.out.println(" ------------ Test nº"+ counter + " ------------ ");
        cache = new Cache(TIME_TO_LIVE, TIMER);

        //Objeto 1
        String key = "Sporting";
        Integer value = 1000000000;

        //Objeto 2
        String key2 = "Benfica";
        Integer value2 = -1;

        //Objeto 3
        String key3 = "Porto";
        Integer value3 = 0;

        cache.put(key,value);
        cache.put(key2,value2);
        cache.put(key3,value3);
    }

    @AfterEach
    void tearDown() {
        cache.cleanup();
        cache.clean();
        counter++;
    }

    @AfterAll
    static void info() {
        System.out.println(counter-1 == 10? "Success: Total of 10 tests passed":"Attention: some tests failed.");
    }

    @Test
    @DisplayName("Cache instantiation Test: Add invalid timer and TTL values, then exception")
    void cacheInstanceTest() {
        assertThrows(IllegalArgumentException.class, new Executable() {
            @Override
            public void execute() throws Throwable {
                cache = new Cache(0, -5);
            }
        });
    }

    @Test
    @DisplayName("Add and Get Test: Put in beforeEach and get inside test")
    void Add_GetTest() {
        assertEquals(1000000000, cache.get("Sporting"));
        assertEquals(-1, cache.get("Benfica"));
        assertEquals(0, cache.get("Porto"));
    }


    @Test
    @DisplayName("Size Test: Put and then size")
    void sizeTest() {
        assertEquals(3, cache.size());
    }

    @Test
    @DisplayName("Empty Test: new cache and then size")
    void isEmptyTest() {
        cache = new Cache(12, 2);
        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("Remove Test: Put and then remove, check size and check null on get")
    void removeTest() {
        cache.remove("Benfica");
        assertNull(cache.get("Benfica"));
        assertNotEquals(null, cache.get("Sporting"));
        assertEquals(2, cache.size());
    }

    @Test
    @DisplayName("Remove Test2: Remove object from key that doesn't exist in cache, then exception")
    void removeNonExistentKeyTest() {
        assertThrows(NullPointerException.class, () -> { cache.remove("Rio Ave");},"Warn: empty stack");
    }

    @Test
    @DisplayName("Expired Test: Expire object and then try to get it")
    void objectExpiredTest() throws InterruptedException {
        Thread.sleep((TIMER + 5) * 1000);
        assertNull(cache.get("Sporting"));
        assertEquals(0, cache.size());
    }

    @Test
    @DisplayName("Not Expired Test: try to get object not expired")
    void objectNotExpiredTest() throws InterruptedException {
        Thread.sleep(2);
        assertEquals(1000000000, cache.get("Sporting"));
        assertEquals(3, cache.size());
    }

    @Test
    @DisplayName("CleanCache Test: Put some objects in cache, increase hits/misses/requests, then clean it and chek vars to zero")
    void clean() {
        int requests = 0;
        int hits = 0;
        int misses = 0;

        //Scenario 1: Object does not exist -> increase misses and requests
        cache.get("Rio Ave");
        requests++;
        misses++;

        //Scenario 2: Object exists -> increase hits and requests
        cache.get("Sporting");
        requests++;
        hits++;

        //Confirm that the vars where updated in cache
        assertEquals(requests, cache.getRequests());
        assertEquals(hits, cache.getHits());
        assertEquals(misses, cache.getMisses());
        assertEquals(3, cache.size());

        //Reset vars values, delete objects in cache and confirm it
        cache.clean();
        assertEquals(0, cache.size());
        assertEquals(0, cache.getRequests());
        assertEquals(0, cache.getHits());
        assertEquals(0, cache.getMisses());
    }

    @Test
    @DisplayName("Get Cache info: do some cache requests and confirm the resquest, hits and misses values")
    void getStats_Test() {
        //request
        cache.get("Sporting");
        cache.get("Porto");

        assertEquals(2, cache.getRequests());
        assertEquals(2, cache.getHits());
        
        assertEquals(TIME_TO_LIVE, cache.getTimeToLive());
        assertEquals(TIMER, cache.getTimer());

        //misses
        cache.get("Braga");
        assertEquals(1, cache.getMisses());
        assertEquals(3, cache.getRequests());

    }

}