package plusTwo.flight.exceptions;

public class SeatNotAvailableException extends FlightBookingException {

    public SeatNotAvailableException(String message) {
        super(message);
    }
}