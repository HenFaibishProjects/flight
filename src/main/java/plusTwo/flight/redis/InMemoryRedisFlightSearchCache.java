package plusTwo.flight.redis;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import plusTwo.flight.responses.FlightResponse;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class InMemoryRedisFlightSearchCache implements FlightSearchCache {

    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();
    private final Duration ttl;

    public InMemoryRedisFlightSearchCache(@Value("${flight.search-cache.ttl}") Duration ttl) {
        this.ttl = ttl;
    }

    @Override
    public List<FlightResponse> get(String origin, String destination, LocalDate departureDate) {
        String key = cacheKey(origin, destination, departureDate);
        CacheEntry entry = cache.get(key);

        if (entry == null) {
            return null;
        }

        if (!Instant.now().isBefore(entry.expiresAt())) {
            cache.remove(key, entry);
            return null;
        }

        return entry.flights();
    }

    @Override
    public void put(String origin, String destination, LocalDate departureDate, List<FlightResponse> flights) {
        CacheEntry entry = new CacheEntry(List.copyOf(flights), Instant.now().plus(ttl));
        cache.put(cacheKey(origin, destination, departureDate), entry);
    }

    private String cacheKey(String origin, String destination, LocalDate departureDate) {
        return normalize(origin) + ":" + normalize(destination) + ":" + departureDate;
    }

    private String normalize(String value) {
        return value.toUpperCase(Locale.ROOT);
    }

    private record CacheEntry(List<FlightResponse> flights, Instant expiresAt) {
    }
}