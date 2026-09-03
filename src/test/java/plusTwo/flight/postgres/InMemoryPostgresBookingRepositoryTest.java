package plusTwo.flight.postgres;

import org.junit.jupiter.api.Test;
import plusTwo.flight.domain.Booking;
import plusTwo.flight.domain.BookingStatus;
import plusTwo.flight.domain.Passenger;

import java.time.Instant;
import java.time.Duration;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

class InMemoryPostgresBookingRepositoryTest {

    @Test
    void onlyOneConcurrentReservationForTheSameFlightAndSeatSucceeds() throws Exception {
        InMemoryPostgresBookingRepository repository = repository(Duration.ofMinutes(10));
        CountDownLatch start = new CountDownLatch(1);
        ExecutorService executor = Executors.newFixedThreadPool(2);

        try {
            Future<Optional<Booking>> firstResult = executor.submit(() -> {
                start.await();
                return repository.reserveIfAvailable(booking("BK-2001"));
            });
            Future<Optional<Booking>> secondResult = executor.submit(() -> {
                start.await();
                return repository.reserveIfAvailable(booking("BK-2002"));
            });

            start.countDown();

            long successfulReservations = java.util.stream.Stream.of(
                            firstResult.get(5, TimeUnit.SECONDS),
                            secondResult.get(5, TimeUnit.SECONDS)
                    )
                    .filter(Optional::isPresent)
                    .count();

            assertEquals(1, successfulReservations);
            assertEquals(1, repository.findByFlightId("FL-1001").size());
        } finally {
            executor.shutdownNow();
        }
    }

    @Test
    void expiredReservationDoesNotBlockANewReservation() {
        InMemoryPostgresBookingRepository repository = repository(Duration.ZERO);
        repository.reserveIfAvailable(booking("BK-2003"));

        Optional<Booking> result = repository.reserveIfAvailable(booking("BK-2004"));

        assertEquals(true, result.isPresent());
    }

    @Test
    void activeReservationCanBeConfirmedAndContinuesToBlockTheSeat() {
        InMemoryPostgresBookingRepository repository = repository(Duration.ofMinutes(10));
        Booking booking = booking("BK-2005");
        repository.reserveIfAvailable(booking);

        Optional<Booking> confirmed = repository.confirmIfReservationActive(booking.bookingId());
        Optional<Booking> competingReservation = repository.reserveIfAvailable(booking("BK-2006"));

        assertEquals(BookingStatus.CONFIRMED, confirmed.orElseThrow().status());
        assertEquals(true, competingReservation.isEmpty());
    }

    private static InMemoryPostgresBookingRepository repository(Duration ttl) {
        return new InMemoryPostgresBookingRepository(ttl);
    }

    private static Booking booking(String bookingId) {
        return new Booking(
                bookingId,
                "FL-1001",
                new Passenger("PS-2001", "Dana", "Levy", "dana.levy@example.com"),
                "2C",
                BookingStatus.RESERVED,
                Instant.now()
        );
    }
}