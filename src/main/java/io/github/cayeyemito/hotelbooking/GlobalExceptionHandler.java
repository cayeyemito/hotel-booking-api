package io.github.cayeyemito.hotelbooking;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import io.github.cayeyemito.hotelbooking.booking.BookingNotFoundException;
import io.github.cayeyemito.hotelbooking.booking.InvalidBookingException;
import io.github.cayeyemito.hotelbooking.booking.RoomUnavailableException;
import io.github.cayeyemito.hotelbooking.room.RoomNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(RoomNotFoundException.class)
    public ProblemDetail handleRoomNotFound(RoomNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ExceptionHandler(InvalidBookingException.class)
    public ProblemDetail handleInvalidBooking(InvalidBookingException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.UNPROCESSABLE_CONTENT, ex.getMessage());
    }

    @ExceptionHandler(RoomUnavailableException.class)
    public ProblemDetail handleRoomUnavailable(RoomUnavailableException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ProblemDetail handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        log.warn("Violación de integridad de datos", ex);
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, "La habitación ya está reservada para esas fechas");
    }

    @ExceptionHandler (BookingNotFoundException.class)
    public ProblemDetail handleBookingNotFound(BookingNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }
}
