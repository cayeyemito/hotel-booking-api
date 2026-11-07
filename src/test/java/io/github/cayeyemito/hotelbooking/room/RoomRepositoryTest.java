package io.github.cayeyemito.hotelbooking.room;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

import io.github.cayeyemito.hotelbooking.TestcontainersConfiguration;
import io.github.cayeyemito.hotelbooking.booking.BookingRepository;
import io.github.cayeyemito.hotelbooking.booking.Booking;
import io.github.cayeyemito.hotelbooking.booking.BookingStatus;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class RoomRepositoryTest {
    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookingRepository bookingRepository;

    private List<String> numbers(List<Room> rooms) {
        return rooms.stream().map(Room::getNumber).toList();
    }

    @Test
    void findAvailableExcludesRoomWithOverlappingConfirmedBooking() {
        Room room1 = roomRepository.save(
                new Room("T1", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
        roomRepository.save(
                new Room("T2", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
        bookingRepository
                .saveAndFlush(new Booking(room1, "Ana", "ana@example.com",
                        LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 14), (short) 2,
                        new BigDecimal("360.00")));

        List<String> available = numbers(roomRepository.findAvailable(
                LocalDate.of(2027, 3, 12), LocalDate.of(2027, 3, 15), BookingStatus.CONFIRMED));

        assertTrue(available.contains("T2"));
        assertFalse(available.contains("T1"));
    }

    @Test
    void findAvailableIncludesRoomWhenBookingEndsOnCheckIn() {
        Room room1 = roomRepository.save(
                new Room("T1", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
        bookingRepository
                .saveAndFlush(new Booking(room1, "Ana", "ana@example.com",
                        LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 14), (short) 2,
                        new BigDecimal("360.00")));

        assertTrue(numbers(roomRepository.findAvailable(LocalDate.of(2027, 3, 14),
                LocalDate.of(2027, 3, 16), BookingStatus.CONFIRMED)).contains("T1"));
    }

    @Test
    void findAvailableIgnoresCancelledBookings() {
        Room room1 = roomRepository.save(
                new Room("T1", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
        Booking cancelled = bookingRepository.saveAndFlush(new Booking(room1, "Ana", "ana@example.com",
                LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 14), (short) 2,
                new BigDecimal("360.00")));
        cancelled.cancel();
        bookingRepository.saveAndFlush(cancelled);

        assertTrue(numbers(roomRepository.findAvailable(LocalDate.of(2027, 3, 12),
                LocalDate.of(2027, 3, 15), BookingStatus.CONFIRMED)).contains("T1"));
    }

    @Test
    void findAvailableExcludesInactiveRooms() {
        Room room1 = roomRepository.save(
                new Room("T1", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
        room1.deactivate();
        roomRepository.saveAndFlush(room1);

        assertFalse(numbers(roomRepository.findAvailable(LocalDate.of(2027, 3, 14),
                LocalDate.of(2027, 3, 16), BookingStatus.CONFIRMED)).contains("T1"));
    }

    @Test
    void findActiveExcludesInactiveRooms() {
        Room room1 = roomRepository.save(
                new Room("T1", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
        roomRepository.save(
                new Room("T2", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
        room1.deactivate();

        roomRepository.saveAndFlush(room1);

        assertFalse(numbers(roomRepository.findByActiveTrueOrderByNumberAsc()).contains("T1"));
        assertTrue(numbers(roomRepository.findByActiveTrueOrderByNumberAsc()).contains("T2"));
    }
}
