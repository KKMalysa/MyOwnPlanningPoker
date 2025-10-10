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

/**
 * WebSocket controller handling real-time Planning Poker interactions.
 * <p>
 * This controller manages room creation, player sessions, voting events,
 * and broadcasting updates to all connected clients using STOMP over WebSocket.
 */
@Controller
public class PokerController {

    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public PokerController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handles incoming messages from the frontend sent to "/app/poker".
     * <p>
     * Based on the message type, performs one of the following actions:
     * <ul>
     *     <li><b>vote</b> - registers a player's vote</li>
     *     <li><b>reveal</b> - reveals all votes and calculates the average</li>
     *     <li><b>reset</b> - resets the current voting session</li>
     * </ul>
     *
     * @param message the message sent from the client containing action type and payload
     * @param headerAccessor provides access to the WebSocket session headers
     */
    @MessageMapping("/poker")
    public void handleMessage(WebSocketMessage message, SimpMessageHeaderAccessor headerAccessor) {
        String type = message.getType();
        String roomId = message.getRoomId();

        rooms.putIfAbsent(roomId, new Room(roomId, new ConcurrentHashMap<>(), false));
        Room room = rooms.get(roomId);

        // Process the message only if the session is registered to the room
        if (!room.isSessionAllowed(headerAccessor.getSessionId())) {
            System.out.println("Unauthorized session attempt ignored");
            return;
        }

        switch (type) {
            case "vote" -> handleVote(room, message);
            case "reveal" -> handleReveal(room);
            case "reset" -> handleReset(room);
        }

        // Broadcast the updated room state to all subscribed clients
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

    /**
     * Handles a player's request to join a specific room.
     * <p>
     * Registers the player's WebSocket session, adds them to the room's player list,
     * and broadcasts the updated room state to all clients subscribed to this room.
     *
     * @param roomId the ID of the room the player wants to join
     * @param player the joining player's details
     * @param headerAccessor provides access to the WebSocket session
     * @return the updated room state after the player has joined
     */
    @MessageMapping("/join/{roomId}")
    @SendTo("/topic/room/{roomId}")
    public Room join(@DestinationVariable String roomId, Player player, SimpMessageHeaderAccessor headerAccessor) {
        Room room = rooms.computeIfAbsent(roomId, id -> new Room(roomId, new ConcurrentHashMap<>(), false));

        // Register the client's WebSocket session to allow future interactions in this room
        String sessionId = headerAccessor.getSessionId();
        room.registerSession(sessionId);

        room.getPlayers().put(player.getName(), player);

        System.out.println("New player joined: " + player.getName() + " [sessionId=" + sessionId + "]");


        return room;
    }

}
