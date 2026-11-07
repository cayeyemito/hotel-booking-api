package io.github.cayeyemito.hotelbooking.booking;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;

import io.github.cayeyemito.hotelbooking.TestcontainersConfiguration;
import io.github.cayeyemito.hotelbooking.room.Room;
import io.github.cayeyemito.hotelbooking.room.RoomRepository;
import io.github.cayeyemito.hotelbooking.room.RoomType;

@DataJpaTest
@Import(TestcontainersConfiguration.class)
class BookingRepositoryTest {

        @Autowired
        private RoomRepository roomRepository;

        @Autowired
        private BookingRepository bookingRepository;

        @Test
        void rejectsOverlappingConfirmedBookingsForTheSameRoom() {
                Room room = roomRepository.save(
                                new Room("T1", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
                bookingRepository.saveAndFlush(new Booking(room, "Ana", "ana@example.com",
                                LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 14), (short) 2,
                                new BigDecimal("360.00")));

                Booking overlapping = new Booking(room, "Luis", "luis@example.com",
                                LocalDate.of(2027, 3, 12), LocalDate.of(2027, 3, 15), (short) 1,
                                new BigDecimal("270.00"));

                assertThrows(DataIntegrityViolationException.class,
                                () -> bookingRepository.saveAndFlush(overlapping));
        }

        @Test
        void allowsBookingThatStartsOnTheCheckOutDay() {
                Room room = roomRepository.save(
                                new Room("T1", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
                bookingRepository.saveAndFlush(new Booking(room, "Ana", "ana@example.com",
                                LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 14), (short) 2,
                                new BigDecimal("360.00")));
                bookingRepository.saveAndFlush(new Booking(room, "Luis", "luis@example.com",
                                LocalDate.of(2027, 3, 14), LocalDate.of(2027, 3, 16), (short) 1,
                                new BigDecimal("180.00")));
                assertEquals(2, bookingRepository.count());
        }

        @Test
        void cancelledBookingDoesNotBlockTheRoom() {
                Room room = roomRepository.save(
                                new Room("T1", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
                Booking cancelled = bookingRepository.saveAndFlush(new Booking(room, "Ana", "ana@example.com",
                                LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 14), (short) 2,
                                new BigDecimal("360.00")));
                cancelled.cancel();
                bookingRepository.saveAndFlush(cancelled);
                bookingRepository.saveAndFlush(new Booking(room, "Ana", "ana@example.com",
                                LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 14), (short) 2,
                                new BigDecimal("360.00")));
                assertEquals(2, bookingRepository.count());
        }

        @Test
        void allowsSameDatesInDifferentRooms() {
                Room room1 = roomRepository.save(
                                new Room("T1", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
                Room room2 = roomRepository.save(
                                new Room("T2", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
                bookingRepository.saveAndFlush(new Booking(room1, "Ana", "ana@example.com",
                                LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 14), (short) 2,
                                new BigDecimal("360.00")));
                bookingRepository.saveAndFlush(new Booking(room2, "Luis", "luis@example.com",
                                LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 14), (short) 1,
                                new BigDecimal("270.00")));
                assertEquals(2, bookingRepository.count());
        }

        @Test
        void findsOverlappingConfirmedBooking() {
                Room room = roomRepository.save(
                                new Room("T1", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
                bookingRepository.saveAndFlush(new Booking(room, "Ana", "ana@example.com",
                                LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 14), (short) 2,
                                new BigDecimal("360.00")));

                assertTrue(bookingRepository.existsByRoomIdAndStatusAndCheckInBeforeAndCheckOutAfter(
                                room.getId(), BookingStatus.CONFIRMED,
                                LocalDate.of(2027, 3, 15), LocalDate.of(2027, 3, 12)));
        }

        @Test
        void doesNotFindBookingThatEndsOnTheRequestedCheckIn() {
                Room room = roomRepository.save(
                                new Room("T1", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00")));
                bookingRepository.saveAndFlush(new Booking(room, "Ana", "ana@example.com",
                                LocalDate.of(2027, 3, 10), LocalDate.of(2027, 3, 14), (short) 2,
                                new BigDecimal("360.00")));
                assertFalse(bookingRepository.existsByRoomIdAndStatusAndCheckInBeforeAndCheckOutAfter(
                                room.getId(), BookingStatus.CONFIRMED,
                                LocalDate.of(2027, 3, 16), LocalDate.of(2027, 3, 14)));
        }
}
