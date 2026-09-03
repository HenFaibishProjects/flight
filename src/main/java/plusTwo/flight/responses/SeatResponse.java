package plusTwo.flight.responses;

import plusTwo.flight.domain.SeatClass;
import plusTwo.flight.domain.SeatStatus;

public record SeatResponse(
        String seatNumber,
        SeatClass seatClass
) {
}