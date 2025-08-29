package com.example.demo.chat;


import com.google.gson.Gson;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

	private static final String SYSTEM_STRING = "system";
	
    private final Gson gson = new Gson();
    private final ConcurrentHashMap<WebSocketSession, String> idMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String clientId = UUID.randomUUID().toString();
        idMap.put(session, clientId);
        SessionManager.add(clientId, session);

        ChatMessage idMsg = new ChatMessage("id", SYSTEM_STRING, null, clientId, null);
        session.sendMessage(new TextMessage(gson.toJson(idMsg)));

        // Broadcast join sin nickname (opcional)
        ChatMessage joinMsg = new ChatMessage("join", SYSTEM_STRING, null, 
                                            "Nuevo usuario conectado", 
                                            null);
        SessionManager.broadcast(gson.toJson(joinMsg));
    }
    
    @Override
	public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatMessage msg = gson.fromJson(message.getPayload(), ChatMessage.class);
        String senderId = idMap.get(session);
        System.out.println(msg);
        if ("nickname".equals(msg.type)) {
            if (senderId == null || msg.nickname == null || msg.nickname.trim().isEmpty()) {
                System.err.println("❌ No se pudo asignar nickname: senderId o nickname es null o vacío");
                return;
            }

            System.out.println("✅ Nickname recibido: " + msg.nickname + " para ID: " + senderId);
            SessionManager.setNickname(senderId, msg.nickname);
            return;
        }

        msg.from = senderId;
        msg.nickname = SessionManager.getNickname(senderId); // Añade el nickname al mensaje

        if ("public".equals(msg.type)) {
            SessionManager.broadcast(gson.toJson(msg));
        } else if ("private".equals(msg.type) && msg.to != null) {
            SessionManager.sendTo(msg.to, gson.toJson(msg));
        }
    }
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        String clientId = idMap.remove(session);
        if (clientId != null) {
            // Obtener el nickname antes de eliminarlo (si existe)
            String nickname = SessionManager.getNickname(clientId);
            SessionManager.remove(clientId); // Elimina tanto la sesión como el nickname

            // Notificar a todos con el nickname (o "Anónimo" si no tenía)
            String leaveMessage = nickname != null ? 
                                nickname + " ha abandonado el chat" : 
                                "Usuario " + clientId.substring(0, 5) + " ha abandonado";
            
            ChatMessage leaveMsg = new ChatMessage("leave", SYSTEM_STRING, null, leaveMessage, nickname);
            SessionManager.broadcast(gson.toJson(leaveMsg));
        }
    }
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        // No handling required because errors are logged elsewhere

    }
}
