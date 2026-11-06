package io.github.cayeyemito.hotelbooking.booking;

public class InvalidBookingException extends RuntimeException {
    public InvalidBookingException(String message) {
        super(message);
    }
}
