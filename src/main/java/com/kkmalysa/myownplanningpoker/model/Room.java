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
}
