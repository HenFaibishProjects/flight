package plusTwo.flight.domain;

import java.time.Instant;

public record Booking(
        String bookingId,
        String flightId,
        Passenger passenger,
        String seatNumber,
        BookingStatus status,
        Instant createdAt
) {
}