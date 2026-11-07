package io.github.cayeyemito.hotelbooking;

import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import io.github.cayeyemito.hotelbooking.booking.BookingNotFoundException;
import io.github.cayeyemito.hotelbooking.booking.InvalidBookingException;
import io.github.cayeyemito.hotelbooking.booking.RoomUnavailableException;
import io.github.cayeyemito.hotelbooking.room.RoomNotFoundException;

@RestControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

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
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT,
                "La habitación ya está reservada para esas fechas");
    }

    @ExceptionHandler(BookingNotFoundException.class)
    public ProblemDetail handleBookingNotFound(BookingNotFoundException ex) {
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        Map<String, String> errors = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ex.getBody().setDetail("La petición tiene campos no válidos");
        ex.getBody().setProperty("errors", errors);
        return super.handleMethodArgumentNotValid(ex, headers, status, request);
    }
}