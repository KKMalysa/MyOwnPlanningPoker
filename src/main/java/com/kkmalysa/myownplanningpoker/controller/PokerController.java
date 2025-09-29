package com.kkmalysa.myownplanningpoker.controller;

import com.kkmalysa.myownplanningpoker.model.Player;
import com.kkmalysa.myownplanningpoker.model.Room;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class PokerController {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();

    @MessageMapping("/join/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public Room join(@DestinationVariable String roomId, Player player) {
        Room room = rooms.computeIfAbsent(roomId, id -> new Room(id, new ConcurrentHashMap<>(), false));
        room.getPlayers().put(player.getName(), player);
        return room;
    }

    @MessageMapping("/vote/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public Room vote(@DestinationVariable String roomId, Player player) {
        Room room = rooms.get(roomId);
        if (room != null) {
            room.getPlayers().put(player.getName(), player);
        }
        return room;
    }

    @MessageMapping("/reveal/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public Room reveal(@DestinationVariable String roomId) {
        Room room = rooms.get(roomId);
        if (room != null) {
            room.setRevealed(true);
        }
        return room;
    }

    @MessageMapping("/reset/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public Room reset(@DestinationVariable String roomId) {
        Room room = rooms.get(roomId);
        if (room != null) {
            room.setRevealed(false);
            room.getPlayers().values().forEach(p -> p.setVote(null));
        }
        return room;
    }
}
