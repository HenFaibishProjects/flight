package plusTwo.flight.postgres;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import plusTwo.flight.domain.Booking;
import plusTwo.flight.domain.BookingStatus;
import plusTwo.flight.domain.Passenger;
import plusTwo.flight.repositories.BookingRepository;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Repository
public class InMemoryPostgresBookingRepository implements BookingRepository {

    private final Map<String, Booking> bookingsById = new ConcurrentHashMap<>();
    private final Map<String, Booking> activeBookingsBySeat = new ConcurrentHashMap<>();
    private final Duration reservationTtl;

    public InMemoryPostgresBookingRepository(@Value("${booking.reservation.ttl}") Duration reservationTtl) {
        this.reservationTtl = reservationTtl;

        List<Booking> sampleBookings = List.of(
            new Booking(
                    "BK-1001",
                    "FL-1002",
                    new Passenger("PS-1001", "Noa", "Levi", "noa.levi@example.com"),
                    "1A",
                    BookingStatus.RESERVED,
                    Instant.parse("2026-09-01T10:00:00Z")
            ),
            new Booking(
                    "BK-1002",
                    "FL-1002",
                    new Passenger("PS-1002", "David", "Cohen", "david.cohen@example.com"),
                    "2B",
                    BookingStatus.CONFIRMED,
                    Instant.parse("2026-09-01T10:15:00Z")
            ),
            new Booking(
                    "BK-1003",
                    "FL-1002",
                    new Passenger("PS-1003", "Maya", "Aviv", "maya.aviv@example.com"),
                    "1B",
                    BookingStatus.CANCELLED,
                    Instant.parse("2026-09-01T10:30:00Z")
            )
        );

        sampleBookings.forEach(booking -> {
            bookingsById.put(booking.bookingId(), booking);
            if (blocksSeat(booking.status()) && !isExpiredReservation(booking)) {
                activeBookingsBySeat.put(reservationKey(booking), booking);
            }
        });
    }

    @Override
    public Booking save(Booking booking) {
        throw new UnsupportedOperationException("Booking creation is not implemented yet");
    }

    @Override
    public Optional<Booking> reserveIfAvailable(Booking booking) {
        AtomicReference<Booking> reservedBooking = new AtomicReference<>();

        activeBookingsBySeat.compute(reservationKey(booking), (key, existingBooking) -> {
            if (existingBooking == null || isExpiredReservation(existingBooking)) {
                reservedBooking.set(booking);
                return booking;
            }

            return existingBooking;
        });

        if (reservedBooking.get() == null) {
            return Optional.empty();
        }

        bookingsById.put(booking.bookingId(), booking);
        return Optional.of(booking);
    }

    @Override
    public Optional<Booking> confirmIfReservationActive(String bookingId) {
        Booking booking = bookingsById.get(bookingId);

        if (booking == null || booking.status() != BookingStatus.RESERVED) {
            return Optional.empty();
        }

        AtomicReference<Booking> confirmedBooking = new AtomicReference<>();

        activeBookingsBySeat.compute(reservationKey(booking), (key, activeBooking) -> {
            if (activeBooking == null || !activeBooking.bookingId().equals(bookingId)) {
                return activeBooking;
            }

            if (activeBooking.status() != BookingStatus.RESERVED) {
                return activeBooking;
            }

            if (isExpiredReservation(activeBooking)) {
                return null;
            }

            Booking confirmed = withStatus(activeBooking, BookingStatus.CONFIRMED);
            bookingsById.put(bookingId, confirmed);
            confirmedBooking.set(confirmed);
            return confirmed;
        });

        return Optional.ofNullable(confirmedBooking.get());
    }

    @Override
    public Optional<Booking> findById(String bookingId) {
        return Optional.ofNullable(bookingsById.get(bookingId));
    }

    @Override
    public List<Booking> findByFlightId(String flightId) {
        List<Booking> flightBookings = bookingsById.values()
                .stream()
                .filter(booking -> booking.flightId().equals(flightId))
                .toList();

        flightBookings.stream()
                .filter(this::isExpiredReservation)
                .forEach(booking -> activeBookingsBySeat.remove(reservationKey(booking), booking));

        return flightBookings.stream()
                .filter(booking -> !isExpiredReservation(booking))
                .toList();
    }

    private static boolean blocksSeat(BookingStatus status) {
        return status == BookingStatus.RESERVED || status == BookingStatus.CONFIRMED;
    }

    private static String reservationKey(Booking booking) {
        return booking.flightId() + ":" + booking.seatNumber();
    }

    private boolean isExpiredReservation(Booking booking) {
        return booking.status() == BookingStatus.RESERVED
                && !booking.createdAt().plus(reservationTtl).isAfter(Instant.now());
    }

    private static Booking withStatus(Booking booking, BookingStatus status) {
        return new Booking(
                booking.bookingId(),
                booking.flightId(),
                booking.passenger(),
                booking.seatNumber(),
                status,
                booking.createdAt()
        );
    }
}