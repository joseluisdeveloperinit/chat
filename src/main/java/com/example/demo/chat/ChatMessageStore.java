package com.example.demo.chat;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;


public class ChatMessageStore {
    private static final int MAX_MESSAGES = 100; // Límite para evitar sobrecarga
    private static final Queue<ChatMessage> PUBLIC_MESSAGES = new ConcurrentLinkedQueue<>();

    public static void addPublicMessage(ChatMessage message) {
        message.timestamp = System.currentTimeMillis();
        if (PUBLIC_MESSAGES.size() >= MAX_MESSAGES) {
            PUBLIC_MESSAGES.poll();
        }
        PUBLIC_MESSAGES.add(message);
    }

    public static List<ChatMessage> getPublicMessages() {
        return new ArrayList<>(PUBLIC_MESSAGES);
    }
}