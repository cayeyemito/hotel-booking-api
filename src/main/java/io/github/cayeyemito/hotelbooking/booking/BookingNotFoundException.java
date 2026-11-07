package io.github.cayeyemito.hotelbooking.booking;

public class BookingNotFoundException extends RuntimeException {
    public BookingNotFoundException(String message) {
        super(message);
    }
    
}
