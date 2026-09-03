package plusTwo.flight.domain;

public record Passenger(
        String passengerId,
        String passportNumber,
        String firstName,
        String lastName,
        String email
) {
}