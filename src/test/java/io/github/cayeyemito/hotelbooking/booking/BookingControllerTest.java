package io.github.cayeyemito.hotelbooking.booking;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.math.BigDecimal;
import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

import io.github.cayeyemito.hotelbooking.room.Room;
import io.github.cayeyemito.hotelbooking.room.RoomNotFoundException;
import io.github.cayeyemito.hotelbooking.room.RoomType;

@WebMvcTest(BookingController.class)
class BookingControllerTest {

    private static final String VALID_REQUEST = """
            {
                "roomId": 1,
                "guestName": "Ana",
                "guestEmail": "ana@example.com",
                "checkIn": "2027-03-06",
                "checkOut": "2027-03-09",
                "guests": 2
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BookingService bookingService;

    @Test
    void createReturns201WithLocationAndBody() throws Exception {
        when(bookingService.create(any(CreateBookingRequest.class))).thenReturn(booking(42L));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/bookings/42"))
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void createReturns400WithFieldErrorsWhenBodyIsEmpty() throws Exception {
        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.roomId").exists())
                .andExpect(jsonPath("$.errors.guestName").exists())
                .andExpect(jsonPath("$.errors.guestEmail").exists())
                .andExpect(jsonPath("$.errors.checkIn").exists())
                .andExpect(jsonPath("$.errors.checkOut").exists())
                .andExpect(jsonPath("$.errors.guests").exists());
        verifyNoInteractions(bookingService);
    }

    @Test
    void createReturns404WhenRoomDoesNotExist() throws Exception {
        when(bookingService.create(any(CreateBookingRequest.class)))
                .thenThrow(new RoomNotFoundException("No existe la habitación 1"));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe la habitación 1"));
    }

    @Test
    void createReturns409WhenRoomIsUnavailable() throws Exception {
        when(bookingService.create(any(CreateBookingRequest.class)))
                .thenThrow(new RoomUnavailableException("La habitación 1 no está disponible"));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("La habitación 1 no está disponible"));
    }

    @Test
    void createReturns422WhenBookingIsInvalid() throws Exception {
        when(bookingService.create(any(CreateBookingRequest.class)))
                .thenThrow(new InvalidBookingException("La reserva no es válida"));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.detail").value("La reserva no es válida"));
    }

    @Test
    void createReturns409WithoutSqlDetailsOnIntegrityViolation() throws Exception {
        when(bookingService.create(any(CreateBookingRequest.class))).thenThrow(
                new DataIntegrityViolationException("could not execute statement [ERROR: conflicting key value violates exclusion constraint \"bookings_no_overlap\"]"));

        mockMvc.perform(post("/bookings")
                .contentType(MediaType.APPLICATION_JSON)
                .content(VALID_REQUEST))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("La habitación ya está reservada para esas fechas"));
    }

    @Test
    void getByIdReturns200WithBooking() throws Exception {
        when(bookingService.findById(42L)).thenReturn(booking(42L));

        mockMvc.perform(get("/bookings/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("CONFIRMED"));
    }

    @Test
    void getByIdReturns404WhenBookingDoesNotExist() throws Exception {
        when(bookingService.findById(42L)).thenThrow(
                new BookingNotFoundException("No existe la reserva 42"));

        mockMvc.perform(get("/bookings/42"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.detail").value("No existe la reserva 42"));
    }

    @Test
    void cancelReturns200WithCancelledStatus() throws Exception {
        Booking booking = booking(42L);
        booking.cancel();
        when(bookingService.cancel(42L)).thenReturn(booking);

        mockMvc.perform(post("/bookings/42/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(42))
                .andExpect(jsonPath("$.status").value("CANCELLED"));
    }

    private Booking booking(Long id) {
        Room room = new Room("101", RoomType.DOUBLE, (short) 2, new BigDecimal("90.00"));
        ReflectionTestUtils.setField(room, "id", 1L);
        Booking booking = new Booking(room, "Ana", "ana@example.com",
                LocalDate.of(2027, 3, 6), LocalDate.of(2027, 3, 9), (short) 2, new BigDecimal("270.00"));
        ReflectionTestUtils.setField(booking, "id", id);
        return booking;
    }
}
