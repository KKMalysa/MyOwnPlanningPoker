package com.kkmalysa.myownplanningpoker.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Room {
    private String id;
    private Map<String, Player> players = new ConcurrentHashMap<>();
    private boolean revealed = false;

    public double calculateAverageVote() {
        return players.values().stream()
                .filter(p -> p.getVote() != null) // non-null votes only
                .mapToInt(Player::getVote)
                .average()
                .orElse(0.0);
    }
}
