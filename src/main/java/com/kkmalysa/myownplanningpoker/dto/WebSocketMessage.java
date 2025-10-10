package com.kkmalysa.myownplanningpoker.dto;

import lombok.Data;

/**
 * Data Transfer Object representing a WebSocket message exchanged between
 * the frontend and backend in the Planning Poker application.
 *
 * <p>It carries information about the action type, the room it belongs to,
 * the player sending the message, and (optionally) the vote value.</p>
 */
@Data
public class WebSocketMessage {
    private String type;      // vote / reveal / reset
    private String roomId;
    private String player;
    private Integer vote;
}

