package com.example.demo;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.handler;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.example.demo.chat.ChatMessage;
import com.example.demo.chat.ChatWebSocketHandler;
import com.example.demo.chat.SessionManager;
import com.google.gson.Gson;

class ChatWebSocketHandlerTest {

    private ChatWebSocketHandler handler;
    private WebSocketSession mockSession;
    private final Gson gson = new Gson();

    @BeforeEach
    void setUp() {
        handler = new ChatWebSocketHandler();
        mockSession = mock(WebSocketSession.class);
        when(mockSession.getId()).thenReturn("session-123");
    }

    @Test
    void testAfterConnectionEstablished() throws Exception {
        handler.afterConnectionEstablished(mockSession);
        
        verify(mockSession, atLeastOnce()).sendMessage(any(TextMessage.class));
        assertTrue(SessionManager.getConnectedUsers().size() > 0);
    }

    @Test
    void testHandleTextMessageWithNickname() throws Exception {
        handler.afterConnectionEstablished(mockSession);
        
        ChatMessage nicknameMsg = new ChatMessage("nickname", null, null, null, "testUser");
        TextMessage textMessage = new TextMessage(gson.toJson(nicknameMsg));
        
        handler.handleTextMessage(mockSession, textMessage);
        
        // Verify nickname was set (you might need to track the session ID)
        assertNotNull(SessionManager.getNickname("some-id")); // Adjust based on actual ID
    }

    @Test
    void testHandleTextMessageWithPublicMessage() throws Exception {
        handler.afterConnectionEstablished(mockSession);
        
        // First set a nickname
        ChatMessage nicknameMsg = new ChatMessage("nickname", null, null, null, "testUser");
        handler.handleTextMessage(mockSession, new TextMessage(gson.toJson(nicknameMsg)));
        
        // Then send public message
        ChatMessage publicMsg = new ChatMessage("public", null, null, "Hello everyone", null);
        handler.handleTextMessage(mockSession, new TextMessage(gson.toJson(publicMsg)));
        
        // Verify broadcast was called (indirectly)
        // This test might need mocking of SessionManager
    }

    @Test
    void testHandleTextMessageWithPrivateMessage() throws Exception {
        // Similar to public message test but for private messages
    }

    @Test
    void testAfterConnectionClosed() throws Exception {
        handler.afterConnectionEstablished(mockSession);
        String sessionId = SessionManager.getConnectedUsers().iterator().next();
        
        SessionManager.setNickname(sessionId, "testUser");
        
        handler.afterConnectionClosed(mockSession, CloseStatus.NORMAL);
        
        assertFalse(SessionManager.getConnectedUsers().contains(sessionId));
    }

    @Test
    void testHandleTransportError() {
        // Should not throw exceptions
        assertDoesNotThrow(() -> handler.handleTransportError(mockSession, new RuntimeException("test error")));
    }
}