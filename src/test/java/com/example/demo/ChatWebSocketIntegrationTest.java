package com.example.demo;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatWebSocketIntegrationTest {

    @LocalServerPort
    private int port;

    @Test
    void testWebSocketConnection() throws Exception {
        CompletableFuture<String> future = new CompletableFuture<>();
        
        StandardWebSocketClient client = new StandardWebSocketClient();
        WebSocketSession session = client.execute(
            new TextWebSocketHandler() {
                @Override
                protected void handleTextMessage(WebSocketSession session, TextMessage message) {
                    future.complete(message.getPayload());
                }
            },
            "ws://localhost:" + port + "/chat"
        ).get();

        // Wait for the ID message
        String message = future.get(5, TimeUnit.SECONDS);
        assertNotNull(message);
        assertTrue(message.contains("id"));

        session.close();
    }
}