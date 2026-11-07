package io.github.cayeyemito.hotelbooking.room;

import java.math.BigDecimal;

public record RoomResponse(
        Long id,
        String number,
        RoomType type,
        short capacity,
        BigDecimal pricePerNight) {
    public static RoomResponse from(Room room) {
        return new RoomResponse(
                room.getId(),
                room.getNumber(),
                room.getType(),
                room.getCapacity(),
                room.getPricePerNight());
    }
}
