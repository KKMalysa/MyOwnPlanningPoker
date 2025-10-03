package com.kkmalysa.myownplanningpoker.controller;

import com.kkmalysa.myownplanningpoker.dto.WebSocketMessage;
import com.kkmalysa.myownplanningpoker.model.Player;
import com.kkmalysa.myownplanningpoker.model.Room;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
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
    public void handleMessage(WebSocketMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String type = message.getType();
        String roomId = message.getRoomId();

        rooms.putIfAbsent(roomId, new Room(roomId, new ConcurrentHashMap<>(), false));
        Room room = rooms.get(roomId);

        // registered sessions only
        if (!room.isSessionAllowed(headerAccessor.getSessionId())) {
            System.out.println("Unauthorized session attempt ignored");
            return;
        }

        switch (type) {
            case "vote" -> handleVote(room, message);
            case "reveal" -> handleReveal(room);
            case "reset" -> handleReset(room);
        }

        // room status update after any action
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

    @MessageMapping("/join/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public Room join(@DestinationVariable String roomId, Player player, SimpMessageHeaderAccessor headerAccessor) {
        Room room = rooms.computeIfAbsent(roomId, id -> new Room());

        // Register safe session
        String sessionId = headerAccessor.getSessionId();
        room.registerSession(sessionId);

        room.getPlayers().put(player.getName(), player);

        System.out.println("✅ New player joined: " + player.getName() + " [sessionId=" + sessionId + "]");


        return room;
    }

}
