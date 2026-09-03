package plusTwo.flight.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import plusTwo.flight.redis.FlightSearchCache;
import plusTwo.flight.domain.Aircraft;
import plusTwo.flight.domain.BookingStatus;
import plusTwo.flight.domain.Flight;
import plusTwo.flight.exceptions.FlightNotFoundException;
import plusTwo.flight.mappers.FlightMapper;
import plusTwo.flight.mappers.SeatMapper;
import plusTwo.flight.repositories.AircraftRepository;
import plusTwo.flight.repositories.BookingRepository;
import plusTwo.flight.repositories.FlightRepository;
import plusTwo.flight.requests.FlightSearchRequest;
import plusTwo.flight.responses.FlightResponse;
import plusTwo.flight.responses.SeatResponse;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class FlightService {

    private static final Logger LOGGER = LoggerFactory.getLogger(FlightService.class);

    private final FlightRepository flightRepository;
    private final FlightMapper flightMapper;
    private final FlightSearchCache flightSearchCache;
    private final AircraftRepository aircraftRepository;
    private final BookingRepository bookingRepository;
    private final SeatMapper seatMapper;

    public FlightService(
            FlightRepository flightRepository,
            FlightMapper flightMapper,
            FlightSearchCache flightSearchCache,
            AircraftRepository aircraftRepository,
            BookingRepository bookingRepository,
            SeatMapper seatMapper
    ) {
        this.flightRepository = flightRepository;
        this.flightMapper = flightMapper;
        this.flightSearchCache = flightSearchCache;
        this.aircraftRepository = aircraftRepository;
        this.bookingRepository = bookingRepository;
        this.seatMapper = seatMapper;
    }

    public List<FlightResponse> searchFlights(FlightSearchRequest request) {
        List<FlightResponse> cachedFlights = flightSearchCache.get(
                request.origin(),
                request.destination(),
                request.departureDate()
        );

        if (cachedFlights != null) {
            LOGGER.info(
                    "[component=FlightService][action=cacheHit] origin={} destination={} departureDate={}",
                    request.origin(),
                    request.destination(),
                    request.departureDate()
            );
            return cachedFlights;
        }

        LOGGER.info(
                "[component=FlightService][action=cacheMiss] origin={} destination={} departureDate={}",
                request.origin(),
                request.destination(),
                request.departureDate()
        );

        List<FlightResponse> flights = flightRepository.search(request.origin(), request.destination(), request.departureDate())
                .stream()
                .map(flightMapper::toResponse)
                .toList();

        flightSearchCache.put(request.origin(), request.destination(), request.departureDate(), flights);

        return flights;
    }

    public List<SeatResponse> getAvailableSeats(String flightId) {
        LOGGER.info("[component=FlightService][action=availableSeats] flightId={}", flightId);

        Flight flight = flightRepository.findById(flightId)
                .orElseThrow(() -> new FlightNotFoundException("Flight not found: " + flightId));

        Aircraft aircraft = aircraftRepository.findById(flight.aircraftId())
                .orElseThrow(() -> new IllegalStateException("Aircraft not found: " + flight.aircraftId()));

        Set<String> unavailableSeatNumbers = bookingRepository.findByFlightId(flightId)
                .stream()
                .filter(booking -> booking.status() == BookingStatus.RESERVED
                        || booking.status() == BookingStatus.CONFIRMED)
                .map(booking -> booking.seatNumber())
                .collect(Collectors.toSet());

        List<SeatResponse> availableSeats = aircraft.seats()
                .stream()
                .filter(seat -> !unavailableSeatNumbers.contains(seat.seatNumber()))
                .map(seatMapper::toResponse)
                .toList();

        LOGGER.info(
                "[component=FlightService][action=availableSeatsResult] flightId={} seatCount={}",
                flightId,
                availableSeats.size()
        );

        return availableSeats;
    }
}