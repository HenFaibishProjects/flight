package plusTwo.flight.domain;

import java.util.List;

public record Aircraft(
        String aircraftId,
        String model,
        List<Seat> seats
) {
}