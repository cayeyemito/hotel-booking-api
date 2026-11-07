package io.github.cayeyemito.hotelbooking.room;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import io.github.cayeyemito.hotelbooking.booking.BookingStatus;

public interface RoomRepository extends JpaRepository<Room, Long> {
    List<Room> findByActiveTrueOrderByNumberAsc();

    @Query("""
            select r from Room r
            where r.active = true
            and not exists (
                select b from Booking b
                where b.room = r
                    and b.status = :status
                    and b.checkIn < :checkOut
                    and b.checkOut > :checkIn)
            order by r.number
            """)
    List<Room> findAvailable(@Param("checkIn") LocalDate checkIn,
            @Param("checkOut") LocalDate checkOut,
            @Param("status") BookingStatus status);
}
