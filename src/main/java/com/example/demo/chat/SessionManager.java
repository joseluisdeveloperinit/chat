package com.example.demo.chat;


import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class SessionManager {
    private static final ConcurrentHashMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    private static final ConcurrentHashMap<String, String> nicknames = new ConcurrentHashMap<>();

    public static void setNickname(String id, String nickname) {
    	
        nicknames.put(id, nickname);
    }

    public static String getNickname(String id) {
        return nicknames.getOrDefault(id, id); //el segundo id es el Valor por defecto
    }
    
    public static void add(String id, WebSocketSession session) {
        sessions.put(id, session);
    }

    public static void remove(String id) {
        sessions.remove(id);
        nicknames.remove(id); // Asegúrate de que esto exista en SessionManager
    }
    
    public static Set<String> getConnectedUsers() {
        return sessions.keySet(); // Retorna los IDs de las sesiones activas.
    }

    public static void sendTo(String id, String message) {
        WebSocketSession session = sessions.get(id);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception ignored) {}
        }
    }

    public static void broadcast(String message) {
        sessions.forEach((id, session) -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                }
            } catch (Exception ignored) {}
        });
    }
}
