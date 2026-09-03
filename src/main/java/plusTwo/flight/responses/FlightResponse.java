package plusTwo.flight.responses;

import java.time.LocalDateTime;

public record FlightResponse(
        String flightId,
        String flightNumber,
        String origin,
        String destination,
        LocalDateTime departureTime,
        LocalDateTime arrivalTime,
        String aircraftId
) {
}