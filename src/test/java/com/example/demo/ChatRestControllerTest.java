package com.example.demo;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.socket.WebSocketSession;

import com.example.demo.chat.SessionManager;
import com.example.demo.controller.ChatRestController;

class ChatRestControllerTest {

    private ChatRestController controller;
    private WebSocketSession mockSession;

    @BeforeEach
    void setUp() {
        controller = new ChatRestController();
        mockSession = mock(WebSocketSession.class);
        
        // Clear any existing sessions
        SessionManager.getConnectedUsers().forEach(SessionManager::remove);
    }

    @Test
    void testGetConnectedUsers() {
        SessionManager.add("user1", mockSession);
        SessionManager.add("user2", mockSession);
        
        Set<String> users = controller.getConnectedUsers();
        
        assertEquals(2, users.size());
        assertTrue(users.contains("user1"));
        assertTrue(users.contains("user2"));
    }

    @Test
    void testGetConnectedUsersWhenEmpty() {
        Set<String> users = controller.getConnectedUsers();
        assertTrue(users.isEmpty());
    }

    @Test
    void testGetServerStatus() {
        String status = controller.getServerStatus();
        assertEquals("Chat server is running (Anonymous mode, no logs)", status);
    }
}