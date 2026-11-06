package io.github.cayeyemito.hotelbooking.booking;

import java.time.LocalDate;

public record CreateBookingRequest(
        Long roomId, String guestName, String guestEmail,
        LocalDate checkIn, LocalDate checkOut, short guests) {
}
