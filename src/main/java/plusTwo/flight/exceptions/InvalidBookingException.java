package plusTwo.flight.exceptions;

public class InvalidBookingException extends FlightBookingException {

    public InvalidBookingException(String message) {
        super(message);
    }
}