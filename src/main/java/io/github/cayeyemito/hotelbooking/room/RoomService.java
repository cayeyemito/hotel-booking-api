package io.github.cayeyemito.hotelbooking.room;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.cayeyemito.hotelbooking.booking.BookingStatus;
import io.github.cayeyemito.hotelbooking.booking.InvalidBookingException;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    @Transactional(readOnly = true)
    public List<Room> findActive() {
        return roomRepository.findByActiveTrueOrderByNumberAsc();
    }

    @Transactional(readOnly = true)
    public List<Room> findAvailable(LocalDate checkIn, LocalDate checkOut) {
        if (!checkOut.isAfter(checkIn)) {
            throw new InvalidBookingException("La fecha de salida debe ser posterior a la fecha de entrada");
        }
        return roomRepository.findAvailable(
                checkIn, checkOut, BookingStatus.CONFIRMED);
    }
}
