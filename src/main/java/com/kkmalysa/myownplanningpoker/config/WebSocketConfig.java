package com.kkmalysa.myownplanningpoker.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * WebSocket configuration for the Planning Poker application.
 * <p>
 * Enables STOMP messaging over WebSocket and defines the messaging endpoints:
 * - /topic: prefix used by the server to send messages to subscribed clients
 * - /app: prefix used by clients when sending messages to the server
 * - /poker: STOMP endpoint that frontend clients use to establish a WebSocket connection
 */
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // /topic - destination prefix used for messages sent from the server to subscribed clients
        config.enableSimpleBroker("/topic");
        // /app -> prefix of paths, that clients send to server
        config.setApplicationDestinationPrefixes("/app");
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        // endpoint WebSocket, for frontend to connect to
        registry.addEndpoint("/poker").setAllowedOriginPatterns("*").withSockJS();
    }
}