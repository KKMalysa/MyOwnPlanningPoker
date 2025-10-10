package com.kkmalysa.myownplanningpoker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Represents a planning poker room/session where players can join,
 * cast their votes, and reveal results in real time.
 *
 * <p>A room maintains the list of participating players, tracks which
 * sessions are authorized to send messages, and stores the current
 * revealed state of the voting round.</p>
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    private String id;
    /** Map of players currently participating in the session (key: player name). */
    private Map<String, Player> players = new ConcurrentHashMap<>();
    /** Indicates whether votes have been revealed to all participants. */
    private boolean revealed = false;
    /** Set of session IDs authorized to send messages within this room. */
    private Set<String> allowedSessions = ConcurrentHashMap.newKeySet();

    public Room(String id, Map<String, Player> players, boolean revealed) {
        this.id = id;
        this.players = players;
        this.revealed = revealed;
        this.allowedSessions = ConcurrentHashMap.newKeySet();
    }

    /**
     * Registers a new WebSocket session ID as authorized for this room.
     *
     * @param sessionId the unique session identifier from the WebSocket connection
     */
    public void registerSession(String sessionId) {
        allowedSessions.add(sessionId);
    }

    /**
     * Checks whether a given WebSocket session is authorized to interact with this room.
     *
     * @param sessionId the session ID to check
     * @return {@code true} if the session is allowed, {@code false} otherwise
     */
    public boolean isSessionAllowed(String sessionId) {
        return allowedSessions.contains(sessionId);
    }

    /**
     * Calculates the average vote of all players in the room.
     *
     * <p>Only non-null votes are included in the calculation. If no votes have been
     * submitted, the result is {@code 0.0}.</p>
     *
     * @return the average of vote values
     */
    public double calculateAverageVote() {
        return players.values().stream()
                .filter(p -> p.getVote() != null) // non-null votes only
                .mapToInt(Player::getVote)
                .average()
                .orElse(0.0);
    }

    public int getParticipantCount() {
        return players.size();
    }

}
