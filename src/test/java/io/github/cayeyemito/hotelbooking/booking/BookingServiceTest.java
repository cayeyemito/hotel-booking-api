package io.github.cayeyemito.hotelbooking.booking;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import io.github.cayeyemito.hotelbooking.room.Room;
import io.github.cayeyemito.hotelbooking.room.RoomNotFoundException;
import io.github.cayeyemito.hotelbooking.room.RoomRepository;
import io.github.cayeyemito.hotelbooking.room.RoomType;

@ExtendWith(MockitoExtension.class)
class BookingServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 3, 1);

    @Mock
    private RoomRepository roomRepository;

    @Mock
    private BookingRepository bookingRepository;

    private BookingService service;
    private Room room;

    @BeforeEach
    void setUp() {
        Clock clock = Clock.fixed(Instant.parse("2027-03-01T10:00:00Z"), ZoneOffset.UTC);
        service = new BookingService(roomRepository, bookingRepository, clock);
        room = new Room("101", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00"));
        ReflectionTestUtils.setField(room, "id", 1L);
    }

    @Test
    void createsBookingWithTotalPriceForAllNights() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.save(any(Booking.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Booking booking = service.create(request(TODAY.plusDays(5), TODAY.plusDays(8), (short) 2));

        assertEquals(new BigDecimal("270.00"), booking.getTotalPrice());
        assertEquals(BookingStatus.CONFIRMED, booking.getStatus());
    }

    @Test
    void rejectsUnknownRoom() {
        when(roomRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(RoomNotFoundException.class,
                () -> service.create(request(TODAY.plusDays(5), TODAY.plusDays(8), (short) 2)));
    }

    @Test
    void rejectsInactiveRoom() {
        room.deactivate();
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(InvalidBookingException.class,
                () -> service.create(request(TODAY.plusDays(5), TODAY.plusDays(8), (short) 2)));
    }

    @Test
    void rejectsCheckOutThatIsNotAfterCheckIn() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(InvalidBookingException.class,
                () -> service.create(request(TODAY.plusDays(5), TODAY.plusDays(5), (short) 2)));
    }

    @Test
    void rejectsCheckInInThePast() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(InvalidBookingException.class,
                () -> service.create(request(TODAY.minusDays(1), TODAY.plusDays(2), (short) 2)));
    }

    @Test
    void rejectsMoreGuestsThanRoomCapacity() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));

        assertThrows(InvalidBookingException.class,
                () -> service.create(request(TODAY.plusDays(5), TODAY.plusDays(8), (short) 3)));
    }

    @Test
    void rejectsRoomThatIsAlreadyBooked() {
        when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
        when(bookingRepository.existsByRoomIdAndStatusAndCheckInBeforeAndCheckOutAfter(
                1L, BookingStatus.CONFIRMED, TODAY.plusDays(8), TODAY.plusDays(5)))
                .thenReturn(true);

        assertThrows(RoomUnavailableException.class,
                () -> service.create(request(TODAY.plusDays(5), TODAY.plusDays(8), (short) 2)));

        verify(bookingRepository, never()).save(any(Booking.class));
    }

    private CreateBookingRequest request(LocalDate checkIn, LocalDate checkOut, short guests) {
        return new CreateBookingRequest(1L, "Ana", "ana@example.com", checkIn, checkOut, guests);
    }
}