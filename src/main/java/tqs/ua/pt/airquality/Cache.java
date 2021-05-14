package tqs.ua.pt.airquality;

import tqs.ua.pt.airquality.Entities.Place;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

// referência: https://crunchify.com/how-to-create-a-simple-in-memory-cache-in-java-lightweight-cache/
    public class Cache<K, T> {

        private final long timeToLive; //tempo em q os objetos guardados em cache são apagados
        private final long timer;
        private Map<K, CacheObject> mycache;    //mapa com os objetos em cache
        private int requests;
        private int hits;
        private int misses;
        private long lastRefresh = System.currentTimeMillis();

        protected class CacheObject extends Place {
            public long lastAccessed = System.currentTimeMillis();
            public final T value;

            protected CacheObject(T value) {
                this.value = value;
            }

        }

        public Cache(long timeToLive, final long timer) {   //construtor
            this.timeToLive = timeToLive * 1000;
            this.timer = timer * 1000;
            this.mycache = new HashMap<>();

            if (timeToLive > 0 && timer > 0) {

                Thread t = new Thread(new Runnable() {
                    public void run() {
                        while (true) {
                            try {
                                Thread.sleep(timer * 1000);
                            } catch (InterruptedException ex) {
                                ex.printStackTrace();
                            }
                            cleanup();
                        }
                    }
                });
                t.setDaemon(true);
                t.start();
            } else
                throw new IllegalArgumentException("Error: TTL and timer values can't be less than 0!!");
        }

        public void put(K key, T value) {
            synchronized (mycache) {
                this.mycache.put(key, new CacheObject(value));
            }
        }

        public T get(K key) {
            synchronized (mycache) {
                CacheObject cacheObject = mycache.get(key);
                this.requests++; //pedido feito e a cache é consultada
                if (cacheObject == null) {   //cache n tinha resultados para aquele pedido
                    this.misses++;
                    return null;
                }
                this.hits++; //foi utilizado o valor guardado em cache em vez de novo pedido à API externa
                cacheObject.lastAccessed = System.currentTimeMillis();
                return cacheObject.value;
            }
        }

        public void remove(String key) {
            synchronized (mycache) {
                if(this.mycache.remove(key) == null)
                    throw new NullPointerException();
            }
        }

        public int size() {
            synchronized (mycache) {
                return mycache.size();
            }
        }


        public void cleanup() {

            long now = System.currentTimeMillis();
            this.lastRefresh = now;

            List<K> expiredObjects = new ArrayList<>();

            synchronized (mycache) {

                for (Map.Entry<K, CacheObject> map : mycache.entrySet()) {

                    CacheObject cacheObject = map.getValue();

                    if (cacheObject != null && now > (timeToLive + cacheObject.lastAccessed)) {
                        expiredObjects.add(map.getKey());
                    }
                }
            }

            for (K key : expiredObjects) {
                synchronized (mycache) {
                    if(this.mycache.remove(key) == null)
                        throw new NullPointerException();
                }

                Thread.yield();
            }
        }

        public void clean() {
            synchronized (mycache) {
                mycache = new HashMap<>();
                this.requests = 0;
                this.hits = 0;
                this.misses = 0;
                this.lastRefresh = System.currentTimeMillis();
            }
        }

        public int getRequests() {
            return requests;
        }

        public int getHits() {
            return hits;
        }

        public int getMisses() {
            return misses;
        }

        public long getTimeToLive() {
            return timeToLive/1000;
        }
        public long getTimer() {
            return timer/1000;
        }

        public long getLastRefresh() {
            return lastRefresh;
        }

        public Map<K, CacheObject> getMyCacheObjs() {
            return mycache;
        }

        public CacheObject findInCacheObjs(Double latitude, Double longitude) {
            for (CacheObject p : this.getMyCacheObjs().values()) {
                if (((Place) p.value).getLatitude().equals(latitude) && ((Place) p.value).getLongitude().equals(longitude))
                    return p;
            }
            return null;
        }
    }