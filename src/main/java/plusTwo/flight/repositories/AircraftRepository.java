package plusTwo.flight.repositories;

import plusTwo.flight.domain.Aircraft;

import java.util.Optional;

public interface AircraftRepository {

    Optional<Aircraft> findById(String aircraftId);
}