package io.github.cayeyemito.hotelbooking.booking;

import java.time.LocalDate;

import org.springframework.data.jpa.repository.JpaRepository;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    boolean existsByRoomIdAndStatusAndCheckInBeforeAndCheckOutAfter(
            Long roomId, BookingStatus status, LocalDate checkOut, LocalDate checkIn);
}