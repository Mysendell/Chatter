package com.chatter.chatter.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;

import com.chatter.chatter.dto.ChatUserStatus;

@Controller
public class ChatWebSocketController {

    // Store online users by chat ID
    private final ConcurrentMap<Integer, Set<String>> onlineUsersPerChat = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @MessageMapping("/chat/online")
    @SendTo("/topic/online")
    public ConcurrentMap<Integer, Set<String>> markUserOnline(ChatUserStatus status) {
        int chatId = status.getChatId();
        String username = status.getUsername();

        System.out.println("User going online: " + username + " in chat: " + chatId);
        onlineUsersPerChat.computeIfAbsent(chatId, k -> ConcurrentHashMap.newKeySet()).add(username);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/online", onlineUsersPerChat.get(chatId));
        return onlineUsersPerChat;
    }

    @MessageMapping("/chat/offline")
    @SendTo("/topic/offline")
    public ConcurrentMap<Integer, Set<String>> markUserOffline(ChatUserStatus status) {
        int chatId = status.getChatId();
        String username = status.getUsername();

        System.out.println("User going offline: " + username + " in chat: " + chatId);
        if (onlineUsersPerChat.containsKey(chatId)) {
            onlineUsersPerChat.get(chatId).remove(username);
            messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/online", onlineUsersPerChat.get(chatId));
        }
        return onlineUsersPerChat;
    }
}