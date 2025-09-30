package com.kkmalysa.myownplanningpoker.dto;

import lombok.Data;

@Data
public class WebSocketMessage {
    private String type;      // vote / reveal / reset
    private String roomId;
    private String player;
    private Integer vote;
}

