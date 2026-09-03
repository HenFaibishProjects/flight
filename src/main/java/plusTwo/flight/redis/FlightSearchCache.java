package plusTwo.flight.redis;

import plusTwo.flight.responses.FlightResponse;

import java.time.LocalDate;
import java.util.List;

public interface FlightSearchCache {

    List<FlightResponse> get(String origin, String destination, LocalDate departureDate);

    void put(String origin, String destination, LocalDate departureDate, List<FlightResponse> flights);
}