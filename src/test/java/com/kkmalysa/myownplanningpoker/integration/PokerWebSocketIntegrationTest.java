package com.kkmalysa.myownplanningpoker.integration;

import com.kkmalysa.myownplanningpoker.MyOwnPlanningPokerApplication;
import com.kkmalysa.myownplanningpoker.dto.WebSocketMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
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

@SpringBootTest(
        classes = MyOwnPlanningPokerApplication.class,
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
class PokerWebSocketIntegrationTest {

    private static final String WS_ENDPOINT = "/poker";
    private static final String ROOM_TOPIC = "/topic/room/test-room";
    private static final String RESULT_TOPIC = "/topic/test-room/result";

    private BlockingQueue<String> blockingQueue;
    private WebSocketStompClient stompClient;

    @BeforeEach
    void setup() {
        blockingQueue = new LinkedBlockingDeque<>();
        List<Transport> transports = List.of(new WebSocketTransport(new StandardWebSocketClient()));
        stompClient = new WebSocketStompClient(new SockJsClient(transports));
        stompClient.setMessageConverter(new MappingJackson2MessageConverter());
    }

    @Test
    void shouldProcessVoteAndRevealCorrectly() throws Exception {
        // 1️⃣ Połącz się z WebSocketem (NOWE API)
        CompletableFuture<StompSession> connectFuture = stompClient.connectAsync(
                "ws://localhost:8080" + WS_ENDPOINT,
                new WebSocketHttpHeaders(),
                new StompSessionHandlerAdapter() {}
        );
        StompSession session = connectFuture.get(5, TimeUnit.SECONDS);

        // 2️⃣ Subskrybuj temat pokoju
        session.subscribe(ROOM_TOPIC, new DefaultStompFrameHandler());
        session.subscribe(RESULT_TOPIC, new DefaultStompFrameHandler());

        // 3️⃣ Wyślij głos (vote)
        WebSocketMessage voteMessage = new WebSocketMessage();
        voteMessage.setType("vote");
        voteMessage.setRoomId("test-room");
        voteMessage.setPlayer("Karol");
        voteMessage.setVote(8);
        session.send("/app/poker", voteMessage);

        // 4️⃣ Odbierz wiadomość o pokoju
        String roomUpdate = blockingQueue.poll(3, TimeUnit.SECONDS);
        assertThat(roomUpdate).contains("Karol");
        assertThat(roomUpdate).contains("8");

        // 5️⃣ Wyślij reveal
        WebSocketMessage revealMessage = new WebSocketMessage();
        revealMessage.setType("reveal");
        revealMessage.setRoomId("test-room");
        session.send("/app/poker", revealMessage);

        // 6️⃣ Odbierz wynik średniej
        String result = blockingQueue.poll(3, TimeUnit.SECONDS);
        assertThat(result).contains("average");
    }


    private class DefaultStompFrameHandler implements StompFrameHandler {
        @Override
        public Type getPayloadType(StompHeaders headers) {
            return String.class;
        }

        @Override
        public void handleFrame(StompHeaders headers, Object payload) {
            blockingQueue.offer(payload.toString());
        }
    }
}
