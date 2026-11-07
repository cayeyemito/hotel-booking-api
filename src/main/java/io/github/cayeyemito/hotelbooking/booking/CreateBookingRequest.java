package io.github.cayeyemito.hotelbooking.booking;

import java.time.LocalDate;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CreateBookingRequest(
        @NotNull Long roomId,
        @NotBlank @Size(max = 255) String guestName,
        @NotBlank @Email @Size(max = 255) String guestEmail,
        @NotNull LocalDate checkIn,
        @NotNull LocalDate checkOut,
        @Positive short guests){
}
