package plusTwo.flight.postgres;

import org.springframework.stereotype.Repository;
import plusTwo.flight.domain.Flight;
import plusTwo.flight.repositories.FlightRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryPostgresFlightRepository implements FlightRepository {

    private final List<Flight> flights = List.of(
            new Flight(
                    "FL-1001",
                    "LY001",
                    "TLV",
                    "JFK",
                    LocalDateTime.of(2026, 10, 12, 0, 45),
                    LocalDateTime.of(2026, 10, 12, 5, 40),
                    "AC-737-01"
            ),
            new Flight(
                    "FL-1002",
                    "LY315",
                    "TLV",
                    "LHR",
                    LocalDateTime.of(2026, 10, 12, 9, 30),
                    LocalDateTime.of(2026, 10, 12, 13, 5),
                    "AC-787-01"
            ),
            new Flight(
                    "FL-1003",
                    "LY316",
                    "LHR",
                    "TLV",
                    LocalDateTime.of(2026, 10, 12, 15, 15),
                    LocalDateTime.of(2026, 10, 12, 22, 0),
                    "AC-787-01"
            ),
            new Flight(
                    "FL-1004",
                    "LY081",
                    "TLV",
                    "BKK",
                    LocalDateTime.of(2026, 10, 13, 22, 0),
                    LocalDateTime.of(2026, 10, 14, 12, 45),
                    "AC-777-01"
            )
    );

    @Override
    public List<Flight> search(String origin, String destination, LocalDate departureDate) {
        return flights.stream()
                .filter(flight -> flight.origin().equalsIgnoreCase(origin))
                .filter(flight -> flight.destination().equalsIgnoreCase(destination))
                .filter(flight -> flight.departureTime().toLocalDate().equals(departureDate))
                .toList();
    }

    @Override
    public Optional<Flight> findById(String flightId) {
        return flights.stream()
                .filter(flight -> flight.flightId().equals(flightId))
                .findFirst();
    }
}
