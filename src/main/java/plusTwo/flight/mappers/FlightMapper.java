package plusTwo.flight.mappers;

import org.mapstruct.Mapper;
import plusTwo.flight.domain.Flight;
import plusTwo.flight.responses.FlightResponse;

@Mapper(componentModel = "spring")
public interface FlightMapper {

    FlightResponse toResponse(Flight flight);
}