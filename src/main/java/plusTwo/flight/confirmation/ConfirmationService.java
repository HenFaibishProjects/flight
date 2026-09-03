package plusTwo.flight.confirmation;

import plusTwo.flight.domain.Booking;

public interface ConfirmationService {

    void sendBookingConfirmation(Booking booking);
}