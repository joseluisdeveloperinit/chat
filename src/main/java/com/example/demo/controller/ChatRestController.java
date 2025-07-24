package com.example.demo.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.chat.SessionManager;

import java.util.Set;

@RestController
@RequestMapping("/api/chat")
public class ChatRestController {

    // Endpoint para obtener la lista de IDs de usuarios conectados
    @GetMapping("/users")
    public Set<String> getConnectedUsers() {
        return SessionManager.getConnectedUsers(); // Necesitarás agregar este método en SessionManager.
    }

    // Endpoint para verificar el estado del servidor
    @GetMapping("/status")
    public String getServerStatus() {
        return "Chat server is running (Anonymous mode, no logs)";
    }
}