package io.github.cayeyemito.hotelbooking.booking;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import io.github.cayeyemito.hotelbooking.room.Room;
import io.github.cayeyemito.hotelbooking.room.RoomNotFoundException;
import io.github.cayeyemito.hotelbooking.room.RoomRepository;

@Service
public class BookingService {

    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final Clock clock;

    public BookingService(RoomRepository roomRepository, BookingRepository bookingRepository, Clock clock) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public Booking findById(Long id) {
        return bookingRepository.findById(id)
                .orElseThrow(() -> new BookingNotFoundException("No existe la reserva " + id));
    }

    @Transactional
    public Booking cancel(Long id) {
        Booking booking = findById(id);
        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new InvalidBookingException("La reserva " + id + " ya está cancelada");
        }
        booking.cancel();
        return booking;
    }

    @Transactional
    public Booking create(CreateBookingRequest request) {
        Room room = roomRepository.findById(request.roomId())
                .orElseThrow(() -> new RoomNotFoundException("No existe la habitación " + request.roomId()));

        if (!room.isActive()) {
            throw new InvalidBookingException("La habitación " + room.getNumber() + " no admite reservas");
        }

        if (!request.checkOut().isAfter(request.checkIn())) {
            throw new InvalidBookingException("La fecha de salida debe ser posterior a la fecha de entrada");
        }
        LocalDate today = LocalDate.now(clock);
        if (request.checkIn().isBefore(today)) {
            throw new InvalidBookingException("La fecha de entrada no puede ser anterior a la fecha actual");
        }

        if (request.guests() > room.getCapacity()) {
            throw new InvalidBookingException("La habitación " + room.getNumber() + " no tiene capacidad para "
                    + request.guests() + " huéspedes");
        }

        if (bookingRepository.existsByRoomIdAndStatusAndCheckInBeforeAndCheckOutAfter(
                room.getId(), BookingStatus.CONFIRMED, request.checkOut(), request.checkIn())) {
            throw new RoomUnavailableException(
                    "La habitación " + room.getNumber() + " no está disponible para las fechas seleccionadas");
        }

        long nights = ChronoUnit.DAYS.between(request.checkIn(), request.checkOut());
        BigDecimal price = room.getPricePerNight().multiply(BigDecimal.valueOf(nights));

        Booking booking = new Booking(room, request.guestName(), request.guestEmail(),
                request.checkIn(), request.checkOut(), request.guests(), price);

        return bookingRepository.save(booking);
    }
}
