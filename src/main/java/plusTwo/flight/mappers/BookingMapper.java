package plusTwo.flight.mappers;

import org.mapstruct.Mapper;
import plusTwo.flight.domain.Booking;
import plusTwo.flight.responses.BookingResponse;

@Mapper(componentModel = "spring")
public interface BookingMapper {

    BookingResponse toResponse(Booking booking);
}