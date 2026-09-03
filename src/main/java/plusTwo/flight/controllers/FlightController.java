package plusTwo.flight.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.web.bind.annotation.*;
import plusTwo.flight.requests.FlightSearchRequest;
import plusTwo.flight.responses.FlightResponse;
import plusTwo.flight.responses.SeatResponse;
import plusTwo.flight.services.FlightService;
import org.springframework.validation.annotation.Validated;

import java.util.List;

@Validated
@RestController
@RequestMapping("/api/flights")
public class FlightController {

    private final FlightService flightService;

    public FlightController(FlightService flightService) {
        this.flightService = flightService;
    }

    @GetMapping("/search")
    public List<FlightResponse> searchFlights(@Valid @ModelAttribute FlightSearchRequest request) {
        return flightService.searchFlights(request);
    }

    @GetMapping("/{flightId}/seats/available")
    public List<SeatResponse> getAvailableSeats(@PathVariable @NotBlank String flightId) {
        return flightService.getAvailableSeats(flightId);
    }
}