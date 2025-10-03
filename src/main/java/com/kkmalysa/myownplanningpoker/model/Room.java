package com.kkmalysa.myownplanningpoker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    private String id;
    private Map<String, Player> players = new ConcurrentHashMap<>();
    private boolean revealed = false;
    private Set<String> allowedSessions = ConcurrentHashMap.newKeySet();

    public Room(String id, Map<String, Player> players, boolean revealed) {
        this.id = id;
        this.players = players;
        this.revealed = revealed;
        this.allowedSessions = ConcurrentHashMap.newKeySet();
    }

    public void registerSession(String sessionId) {
        allowedSessions.add(sessionId);
    }

    public boolean isSessionAllowed(String sessionId) {
        return allowedSessions.contains(sessionId);
    }

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
