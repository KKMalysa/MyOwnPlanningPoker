package com.kkmalysa.myownplanningpoker.integration;

import com.kkmalysa.myownplanningpoker.MyOwnPlanningPokerApplication;
import com.kkmalysa.myownplanningpoker.dto.WebSocketMessage;
import com.kkmalysa.myownplanningpoker.model.Player;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;
import org.springframework.web.socket.sockjs.client.SockJsClient;
import org.springframework.web.socket.sockjs.client.Transport;
import org.springframework.web.socket.sockjs.client.WebSocketTransport;

import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

// This is an integration test, not a unit test.
@SpringBootTest(
        classes = MyOwnPlanningPokerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class PokerWebSocketIntegrationTest {

    private static final String WS_ENDPOINT = "/poker";
    private static final String ROOM_TOPIC = "/topic/room/test-room";
    private static final String RESULT_TOPIC = "/topic/test-room/result";

    @LocalServerPort
    private int port;
    private BlockingQueue<String> blockingQueue;
    private WebSocketStompClient stompClient;

    // Connects to a real running WebSocket endpoint and verifies full STOMP flow end-to-end.
    // configure WebSocket STOMP client
    @BeforeEach
    void setup() {
        blockingQueue = new LinkedBlockingDeque<>();
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        stompClient = new WebSocketStompClient(new SockJsClient(transports));

        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        stompClient.setMessageConverter(converter);
    }

    @Test
    void shouldProcessVoteAndRevealCorrectly() throws Exception {
        // Establish a real STOMP connection to the embedded Spring Boot server
        CompletableFuture<StompSession> connectFuture = stompClient.connectAsync(
                "ws://localhost:" + port + WS_ENDPOINT,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {
                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
                        System.err.println("[WebSocket error] " + exception.getMessage());
                        exception.printStackTrace();
                    }
                }
        );
        StompSession session = connectFuture.get(5, TimeUnit.SECONDS);

        // Subscribe to room updates and results published by the server
        session.subscribe(ROOM_TOPIC, new DefaultStompFrameHandler());
        session.subscribe(RESULT_TOPIC, new DefaultStompFrameHandler());
        Thread.sleep(300); //to do not lose joinAck message

        // Simulate player joining
        Player player = new Player();
        player.setName("Karol");
        player.setVote(null);
        session.send("/app/join/test-room", player);

        String joinAck = blockingQueue.poll(5, TimeUnit.SECONDS);
        assertThat(joinAck).isNotNull();
        assertThat(joinAck).contains("Karol");

        //Simulate voting
        WebSocketMessage voteMessage = new WebSocketMessage();
        voteMessage.setType("vote");
        voteMessage.setRoomId("test-room");
        voteMessage.setPlayer("Karol");
        voteMessage.setVote(8);
        session.send("/app/poker", voteMessage);

        String roomUpdate = blockingQueue.poll(5, TimeUnit.SECONDS);
        assertThat(roomUpdate).isNotNull();
        assertThat(roomUpdate).contains("Karol");
        assertThat(roomUpdate).contains("8");

        //Simulate reveal
        WebSocketMessage revealMessage = new WebSocketMessage();
        revealMessage.setType("reveal");
        revealMessage.setRoomId("test-room");
        session.send("/app/poker", revealMessage);

        String result = blockingQueue.poll(5, TimeUnit.SECONDS);
        assertThat(result).isNotNull();
        assertThat(result).contains("average");
    }

    // Captures messages sent by server and adds them to the blocking queue
    private class DefaultStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return Object.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            String message;
            if (payload instanceof byte[] bytes) {
                message = new String(bytes);
            } else {
                message = payload.toString();
            }

            System.out.println("[MESSAGE RECEIVED] " + message);
            blockingQueue.offer(message);
        }
    }
}
