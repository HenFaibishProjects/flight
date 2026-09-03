package plusTwo.flight.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import plusTwo.flight.confirmation.ConfirmationService;
import plusTwo.flight.domain.Aircraft;
import plusTwo.flight.domain.Booking;
import plusTwo.flight.domain.BookingStatus;
import plusTwo.flight.domain.Flight;
import plusTwo.flight.domain.Passenger;
import plusTwo.flight.exceptions.BookingNotFoundException;
import plusTwo.flight.exceptions.FlightNotFoundException;
import plusTwo.flight.exceptions.InvalidBookingException;
import plusTwo.flight.exceptions.SeatNotAvailableException;
import plusTwo.flight.mappers.BookingMapper;
import plusTwo.flight.repositories.AircraftRepository;
import plusTwo.flight.repositories.BookingRepository;
import plusTwo.flight.repositories.FlightRepository;
import plusTwo.flight.requests.CreateBookingRequest;
import plusTwo.flight.responses.BookingResponse;

import java.time.Instant;
import java.util.UUID;

@Service
public class BookingService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BookingService.class);

    private final FlightRepository flightRepository;
    private final AircraftRepository aircraftRepository;
    private final BookingRepository bookingRepository;
    private final BookingMapper bookingMapper;
    private final ConfirmationService confirmationService;

    public BookingService(
            FlightRepository flightRepository,
            AircraftRepository aircraftRepository,
            BookingRepository bookingRepository,
            BookingMapper bookingMapper,
            ConfirmationService confirmationService
    ) {
        this.flightRepository = flightRepository;
        this.aircraftRepository = aircraftRepository;
        this.bookingRepository = bookingRepository;
        this.bookingMapper = bookingMapper;
        this.confirmationService = confirmationService;
    }

    public BookingResponse createBooking(CreateBookingRequest request) {
        Flight flight = flightRepository.findById(request.flightId())
                .orElseThrow(() -> new FlightNotFoundException("Flight not found: " + request.flightId()));

        Aircraft aircraft = aircraftRepository.findById(flight.aircraftId())
                .orElseThrow(() -> new InvalidBookingException("Aircraft not found for flight: " + request.flightId()));

        boolean seatExists = aircraft.seats()
                .stream()
                .anyMatch(seat -> seat.seatNumber().equals(request.seatNumber()));

        if (!seatExists) {
            throw new InvalidBookingException(
                    "Seat " + request.seatNumber() + " does not exist on flight " + request.flightId()
            );
        }

        Passenger passenger = new Passenger(
                UUID.randomUUID().toString(),
                request.passportNumber(),
                request.passengerFirstName(),
                request.passengerLastName(),
                request.passengerEmail()
        );

        Booking booking = new Booking(
                UUID.randomUUID().toString(),
                request.flightId(),
                passenger,
                request.seatNumber(),
                BookingStatus.RESERVED,
                Instant.now()
        );

        Booking savedBooking = bookingRepository.reserveIfAvailable(booking)
                .orElseThrow(() -> {
                    LOGGER.info(
                            "[component=BookingService][action=seatUnavailable] flightId={} seatNumber={}",
                            request.flightId(),
                            request.seatNumber()
                    );
                    return new SeatNotAvailableException(
                            "Seat " + request.seatNumber() + " is not available on flight " + request.flightId()
                    );
                });

        LOGGER.info(
                "[component=BookingService][action=seatReserved] bookingId={} flightId={} seatNumber={}",
                savedBooking.bookingId(),
                savedBooking.flightId(),
                savedBooking.seatNumber()
        );

        return bookingMapper.toResponse(savedBooking);
    }

    public BookingResponse confirmBooking(String bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + bookingId));

        if (booking.status() == BookingStatus.CONFIRMED) {
            return bookingMapper.toResponse(booking);
        }

        if (booking.status() == BookingStatus.CANCELLED) {
            throw new InvalidBookingException("Cancelled booking cannot be confirmed: " + bookingId);
        }

        return bookingRepository.confirmIfReservationActive(bookingId)
                .map(confirmedBooking -> {
                    LOGGER.info(
                            "[component=BookingService][action=bookingConfirmed] bookingId={} flightId={} seatNumber={}",
                            confirmedBooking.bookingId(),
                            confirmedBooking.flightId(),
                            confirmedBooking.seatNumber()
                    );
                    confirmationService.sendBookingConfirmation(confirmedBooking);
                    return bookingMapper.toResponse(confirmedBooking);
                })
                .orElseGet(() -> handleFailedConfirmation(booking));
    }

    private BookingResponse handleFailedConfirmation(Booking booking) {
        Booking currentBooking = bookingRepository.findById(booking.bookingId())
                .orElseThrow(() -> new BookingNotFoundException("Booking not found: " + booking.bookingId()));

        if (currentBooking.status() == BookingStatus.CONFIRMED) {
            return bookingMapper.toResponse(currentBooking);
        }

        LOGGER.info(
                "[component=BookingService][action=reservationExpired] bookingId={} flightId={} seatNumber={}",
                booking.bookingId(),
                booking.flightId(),
                booking.seatNumber()
        );
        throw new SeatNotAvailableException("Reservation expired for booking: " + booking.bookingId());
    }

    public BookingResponse getBooking(String bookingId) {

        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() ->
                        new BookingNotFoundException(
                                "Booking not found: " + bookingId
                        )
                );

        return bookingMapper.toResponse(booking);
    }
}