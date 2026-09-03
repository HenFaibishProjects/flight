package plusTwo.flight.repositories;

import plusTwo.flight.domain.Flight;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface FlightRepository {

    List<Flight> search(String origin, String destination, LocalDate departureDate);

    Optional<Flight> findById(String flightId);
}