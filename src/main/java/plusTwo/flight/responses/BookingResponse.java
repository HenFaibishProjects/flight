package plusTwo.flight.responses;

import plusTwo.flight.domain.BookingStatus;

import java.time.Instant;

public record BookingResponse(
        String bookingId,
        String flightId,
        String seatNumber,
        BookingStatus status,
        Instant createdAt
) {
}