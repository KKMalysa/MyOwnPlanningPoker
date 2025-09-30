package com.kkmalysa.myownplanningpoker.controller;

import com.kkmalysa.myownplanningpoker.dto.WebSocketMessage;
import com.kkmalysa.myownplanningpoker.model.Player;
import com.kkmalysa.myownplanningpoker.model.Room;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Controller
public class PokerController {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public PokerController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/poker") // frontend
    public void handleMessage(WebSocketMessage message) {
        String type = message.getType();
        String roomId = message.getRoomId();

        rooms.putIfAbsent(roomId, new Room(roomId, new ConcurrentHashMap<>(), false));
        Room room = rooms.get(roomId);

        switch (type) {
            case "vote" -> handleVote(room, message);
            case "reveal" -> handleReveal(room);
            case "reset" -> handleReset(room);
        }

        // Aktualizuj stan pokoju po każdej akcji
        messagingTemplate.convertAndSend("/topic/room/" + roomId, room);
    }

    private void handleVote(Room room, WebSocketMessage message) {
        room.getPlayers().put(
                message.getPlayer(),
                new Player(message.getPlayer(), message.getVote())
        );
    }

    private void handleReveal(Room room) {
        room.setRevealed(true);
        double average = room.calculateAverageVote();
        messagingTemplate.convertAndSend("/topic/" + room.getId() + "/result", Map.of("average", average));
    }

    private void handleReset(Room room) {
        room.setRevealed(false);
        room.getPlayers().values().forEach(p -> p.setVote(null));
    }
}
