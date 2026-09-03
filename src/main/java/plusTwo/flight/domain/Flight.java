package plusTwo.flight.domain;

import java.time.LocalDateTime;

public record Flight(
        String flightId,
        String flightNumber,
        String origin,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        String aircraftId
) {
}