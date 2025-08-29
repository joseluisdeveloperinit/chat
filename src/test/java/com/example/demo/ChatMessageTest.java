package com.example.demo;



import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.demo.chat.ChatMessage;

class ChatMessageTest {

    @Test
    void testDefaultConstructor() {
        ChatMessage message = new ChatMessage();
        assertNull(message.type);
        assertNull(message.from);
        assertNull(message.to);
        assertNull(message.message);
        assertNull(message.nickname);
    }

    @Test
    void testParameterizedConstructor() {
        ChatMessage message = new ChatMessage("public", "user1", "user2", "Hello", "john");
        
        assertEquals("public", message.type);
        assertEquals("user1", message.from);
        assertEquals("user2", message.to);
        assertEquals("Hello", message.message);
        assertEquals("john", message.nickname);
    }

    @Test
    void testNicknameGetterSetter() {
        ChatMessage message = new ChatMessage();
        message.setNickname("testUser");
        
        assertEquals("testUser", message.getNickname());
    }

    @Test
    void testToString() {
        ChatMessage message = new ChatMessage("private", "user1", "user2", "Hi there", "john");
        String result = message.toString();
        
        assertTrue(result.contains("type=private"));
        assertTrue(result.contains("from=user1"));
        assertTrue(result.contains("to=user2"));
        assertTrue(result.contains("message=Hi there"));
        assertTrue(result.contains("nickname=john"));
    }
}