package plusTwo.flight.controllers;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import plusTwo.flight.requests.CreateBookingRequest;
import plusTwo.flight.responses.BookingResponse;
import plusTwo.flight.services.BookingService;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/api/bookings")
public class BookingController {

    private final BookingService bookingService;

    public BookingController(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    @PostMapping("/{bookingId}/confirm")
    public BookingResponse confirmBooking(@PathVariable @NotBlank String bookingId) {
        return bookingService.confirmBooking(bookingId);
    }

    @GetMapping("/{bookingId}")
    public BookingResponse getBooking(@PathVariable @NotBlank String bookingId) {
        return bookingService.getBooking(bookingId);
    }
}