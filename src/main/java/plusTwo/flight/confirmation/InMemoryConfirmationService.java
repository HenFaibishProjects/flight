package plusTwo.flight.confirmation;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import plusTwo.flight.domain.Booking;

@Service
public class InMemoryConfirmationService implements ConfirmationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(InMemoryConfirmationService.class);

    @Override
    public void sendBookingConfirmation(Booking booking) {
        LOGGER.info(
                "[component=InMemoryConfirmationService][action=confirmationSent] "
                        + "bookingId={} flightId={} email={} seatNumber={}",
                booking.bookingId(),
                booking.flightId(),
                booking.passenger().email(),
                booking.seatNumber()
        );
    }
}