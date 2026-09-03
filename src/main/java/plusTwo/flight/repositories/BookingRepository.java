package plusTwo.flight.repositories;

import plusTwo.flight.domain.Booking;

import java.util.List;
import java.util.Optional;

public interface BookingRepository {

    Booking save(Booking booking);

    Optional<Booking> reserveIfAvailable(Booking booking);

    Optional<Booking> confirmIfReservationActive(String bookingId);

    Optional<Booking> findById(String bookingId);

    List<Booking> findByFlightId(String flightId);
}