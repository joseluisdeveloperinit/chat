package com.example.demo.controller;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.chat.ChatMessage;
import com.example.demo.chat.ChatMessageStore;
import com.example.demo.chat.SessionManager;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

	@GetMapping("/users")
	public List<Map<String, String>> getConnectedUsers() {
	    return SessionManager.getConnectedUsers().stream()
	        .map(id -> Map.of(
	            "id", id,
	            "nickname", SessionManager.getNickname(id)
	        ))
	        .collect(Collectors.toList());
	}

    // Endpoint para verificar el estado del servidor
    @GetMapping("/status")
    public String getServerStatus() {
        return "Chat server is running (Anonymous mode, no logs)";
    }
    
    
 // En ChatRestController.java
    @GetMapping("/messages")
    public List<ChatMessage> getPublicMessages() {
        return ChatMessageStore.getPublicMessages().stream()
            .filter(msg -> "public".equals(msg.type)) // Solo mensajes públicos
            .collect(Collectors.toList());
    }
}