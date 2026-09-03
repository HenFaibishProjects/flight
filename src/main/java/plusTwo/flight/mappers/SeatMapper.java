package plusTwo.flight.mappers;

import org.mapstruct.Mapper;
import plusTwo.flight.domain.Seat;
import plusTwo.flight.responses.SeatResponse;

@Mapper(componentModel = "spring")
public interface SeatMapper {

    SeatResponse toResponse(Seat seat);
}