package io.github.cayeyemito.hotelbooking.room;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
public class RoomController {
    private final RoomService roomService;

    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }

    @GetMapping
    public List<RoomResponse> findActive() {
        List<Room> rooms = roomService.findActive();
        return rooms.stream()
                .map(RoomResponse::from)
                .toList();
    }
}
