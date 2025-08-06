package com.example.demo.chat;


import com.google.gson.Gson;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class ChatWebSocketHandler extends TextWebSocketHandler {

    private final Gson gson = new Gson();
    private final ConcurrentHashMap<WebSocketSession, String> idMap = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String clientId = UUID.randomUUID().toString();
        idMap.put(session, clientId);
        SessionManager.add(clientId, session);

        ChatMessage idMsg = new ChatMessage("id", "system", null, clientId, null);
        session.sendMessage(new TextMessage(gson.toJson(idMsg)));

        // Broadcast join sin nickname (opcional)
        ChatMessage joinMsg = new ChatMessage("join", "system", null, 
                                            "Nuevo usuario conectado", 
                                            null);
        SessionManager.broadcast(gson.toJson(joinMsg)); 
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        ChatMessage msg = gson.fromJson(message.getPayload(), ChatMessage.class);
        String senderId = idMap.get(session);
        

        // Validación para imágenes
        if ("image".equals(msg.type)) {
            if (msg.to == null || msg.imageData == null) {
                // Notificar error al remitente
                ChatMessage errorMsg = new ChatMessage("error", "system", senderId, 
                    "Las imágenes solo pueden enviarse en mensajes privados", null);
                session.sendMessage(new TextMessage(gson.toJson(errorMsg)));
                return;
            }
            
            // Verificar que el destinatario existe
            if (!SessionManager.getConnectedUsers().contains(msg.to)) {
                ChatMessage errorMsg = new ChatMessage("error", "system", senderId, 
                    "El usuario destino no está conectado", null);
                session.sendMessage(new TextMessage(gson.toJson(errorMsg)));
                return;
            }
            
            // Añadir metadatos del remitente
            msg.from = senderId;
            msg.nickname = SessionManager.getNickname(senderId);
            
            // Enviar solo al destinatario
            SessionManager.sendTo(msg.to, gson.toJson(msg));
            
            // Confirmar entrega al remitente
            ChatMessage confirmation = new ChatMessage("delivery", "system", senderId, 
                "Imagen enviada a " + SessionManager.getNickname(msg.to), null);
            session.sendMessage(new TextMessage(gson.toJson(confirmation)));
            
            return;
        }

        if ("nickname".equals(msg.type)) {
            if (senderId == null || msg.nickname == null || msg.nickname.trim().isEmpty()) {
                System.err.println("❌ Nickname inválido");
                return;
            }

            // Actualiza el nickname
            SessionManager.setNickname(senderId, msg.nickname);
            
            // Notifica a todos los clientes con la lista actualizada
            broadcastUserList();
            return;
        }

        msg.from = senderId;
        msg.nickname = SessionManager.getNickname(senderId); // Añade el nickname al mensaje

        if ("public".equals(msg.type)) {
            // 1. Primero almacenamos el mensaje
            ChatMessage publicMsg = new ChatMessage(
                "public",
                senderId,
                null, // to es null para mensajes públicos
                msg.message,
                SessionManager.getNickname(senderId)
            );
            ChatMessageStore.addPublicMessage(publicMsg);
            
            // 2. Luego lo transmitimos a todos
            SessionManager.broadcast(gson.toJson(msg));
            
        } else if ("private".equals(msg.type) && msg.to != null) {
            // Verificar si el destinatario existe
            if (SessionManager.getConnectedUsers().contains(msg.to)) {
                // Enviar al destinatario
                SessionManager.sendTo(msg.to, gson.toJson(msg));
                
                // Opcional: Confirmar al emisor que el mensaje fue enviado
                ChatMessage deliveryConfirmation = new ChatMessage(
                    "private-delivery",
                    "system",
                    senderId,
                    "Mensaje privado enviado a " + SessionManager.getNickname(msg.to),
                    null
                );
                session.sendMessage(new TextMessage(gson.toJson(deliveryConfirmation)));
            } else {
                // Notificar al emisor que el destinatario no existe
                ChatMessage errorMsg = new ChatMessage(
                    "error",
                    "system",
                    senderId,
                    "El usuario " + msg.to + " no está conectado",
                    null
                );
                session.sendMessage(new TextMessage(gson.toJson(errorMsg)));
            }
        }
    }
    
    private void broadcastUserList() {
        Set<String> userIds = SessionManager.getConnectedUsers();
        List<Map<String, String>> userList = userIds.stream()
            .map(id -> {
                String nickname = SessionManager.getNickname(id);
                return Map.of(
                    "id", id,
                    "nickname", nickname != null ? nickname : id.substring(0, 5)
                );
            })
            .collect(Collectors.toList());

        ChatMessage userListMsg = new ChatMessage(
            "users-updated",
            "system",
            null,
            gson.toJson(userList), 
            null
        );
        SessionManager.broadcast(gson.toJson(userListMsg));
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
            		          " ha abandonado el chat" : 
                                "Usuario " + clientId.substring(0, 5) + " ha abandonado";
            
            ChatMessage leaveMsg = new ChatMessage("leave", "system", null, leaveMessage, nickname);
            SessionManager.broadcast(gson.toJson(leaveMsg));
        }
    }
    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        exception.printStackTrace();
    }
         
}
