package com.example.demo;


import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import com.example.demo.chat.SessionManager;

class SessionManagerTest {

    private WebSocketSession mockSession;
    private final String testId = "test-id-123";
    private final String testNickname = "testUser";

    @BeforeEach
    void setUp() {
        mockSession = mock(WebSocketSession.class);
        SessionManager.remove(testId); // Clean up before each test
    }

    @Test
    void testAddAndRemoveSession() {
        SessionManager.add(testId, mockSession);
        assertTrue(SessionManager.getConnectedUsers().contains(testId));
        
        SessionManager.remove(testId);
        assertFalse(SessionManager.getConnectedUsers().contains(testId));
    }

    @Test
    void testSetAndGetNickname() {
        SessionManager.setNickname(testId, testNickname);
        assertEquals(testNickname, SessionManager.getNickname(testId));
        
        // Test default value when nickname doesn't exist
        assertEquals("unknown-id", SessionManager.getNickname("unknown-id"));
    }

    @Test
    void testSendToExistingSession() throws IOException {
        SessionManager.add(testId, mockSession);
        when(mockSession.isOpen()).thenReturn(true);
        
        SessionManager.sendTo(testId, "test message");
        
        verify(mockSession, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void testSendToNonExistingSession() {
        assertDoesNotThrow(() -> SessionManager.sendTo("non-existing-id", "message"));
    }

    @Test
    void testSendToClosedSession() throws IOException {
        SessionManager.add(testId, mockSession);
        when(mockSession.isOpen()).thenReturn(false);
        
        SessionManager.sendTo(testId, "test message");
        
        verify(mockSession, never()).sendMessage(any(TextMessage.class));
    }

    @Test
    void testBroadcast() throws IOException {
        WebSocketSession session1 = mock(WebSocketSession.class);
        WebSocketSession session2 = mock(WebSocketSession.class);
        
        SessionManager.add("id1", session1);
        SessionManager.add("id2", session2);
        
        when(session1.isOpen()).thenReturn(true);
        when(session2.isOpen()).thenReturn(true);
        
        SessionManager.broadcast("broadcast message");
        
        verify(session1, times(1)).sendMessage(any(TextMessage.class));
        verify(session2, times(1)).sendMessage(any(TextMessage.class));
    }

    @Test
    void testGetConnectedUsers() {
        SessionManager.add("id1", mockSession);
        SessionManager.add("id2", mockSession);
        
        Set<String> users = SessionManager.getConnectedUsers();
        
        assertEquals(2, users.size());
        assertTrue(users.contains("id1"));
        assertTrue(users.contains("id2"));
    }
}