package io.github.cayeyemito.hotelbooking.room;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import io.github.cayeyemito.hotelbooking.booking.BookingStatus;
import io.github.cayeyemito.hotelbooking.booking.InvalidBookingException;

@ExtendWith(MockitoExtension.class)
class RoomServiceTest {

    private static final LocalDate TODAY = LocalDate.of(2027, 3, 1);

    @Mock
    private RoomRepository roomRepository;

    private RoomService service;

    @BeforeEach
    void setUp() {
        service = new RoomService(roomRepository);
    }

    @Test
    void findAvailableRejectsCheckOutEqualToCheckIn() {
        assertThrows(InvalidBookingException.class,
                () -> service.findAvailable(TODAY, TODAY));

        verifyNoInteractions(roomRepository);
    }

    @Test
    void findAvailableRejectsCheckOutBeforeCheckIn() {
        assertThrows(InvalidBookingException.class,
                () -> service.findAvailable(TODAY, TODAY.minusDays(1)));

        verifyNoInteractions(roomRepository);
    }

    @Test
    void findAvailablePassesConfirmedStatusToRepository() {
        Room room = new Room("101", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00"));
        when(roomRepository.findAvailable(TODAY, TODAY.plusDays(1), BookingStatus.CONFIRMED))
                .thenReturn(List.of(room));

        List<Room> result = service.findAvailable(TODAY, TODAY.plusDays(1));

        assertEquals(List.of(room), result);
    }
}
