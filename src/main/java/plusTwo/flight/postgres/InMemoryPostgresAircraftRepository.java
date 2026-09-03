package plusTwo.flight.postgres;

import org.springframework.stereotype.Repository;
import plusTwo.flight.domain.Aircraft;
import plusTwo.flight.domain.Seat;
import plusTwo.flight.domain.SeatClass;
import plusTwo.flight.repositories.AircraftRepository;

import java.util.List;
import java.util.Optional;

@Repository
public class InMemoryPostgresAircraftRepository implements AircraftRepository {

    private final List<Aircraft> aircraft = List.of(
            new Aircraft("AC-737-01", "Boeing 737-900", seatMap()),
            new Aircraft("AC-787-01", "Boeing 787-9 Dreamliner", seatMap()),
            new Aircraft("AC-777-01", "Boeing 777-200ER", seatMap())
    );

    @Override
    public Optional<Aircraft> findById(String aircraftId) {
        return aircraft.stream()
                .filter(item -> item.aircraftId().equals(aircraftId))
                .findFirst();
    }

    private static List<Seat> seatMap() {
        return List.of(
                new Seat("1A", SeatClass.BUSINESS),
                new Seat("1B", SeatClass.BUSINESS),
                new Seat("1C", SeatClass.BUSINESS),
                new Seat("2A", SeatClass.ECONOMY),
                new Seat("2B", SeatClass.ECONOMY),
                new Seat("2C", SeatClass.ECONOMY)
        );
    }
}