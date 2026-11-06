package io.github.cayeyemito.hotelbooking.booking;

public class RoomUnavailableException extends RuntimeException {
    public RoomUnavailableException(String message) {
        super(message);
    }
}
