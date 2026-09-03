package plusTwo.flight.requests;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record CreateBookingRequest(
        @NotBlank String flightId,
        @NotBlank String seatNumber,
        @NotBlank String passportNumber,
        @NotBlank String passengerFirstName,
        @NotBlank String passengerLastName,
        @Email @NotBlank String passengerEmail
) {
}