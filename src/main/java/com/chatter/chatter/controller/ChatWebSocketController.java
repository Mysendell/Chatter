package com.chatter.chatter.controller;

import com.chatter.chatter.dto.Chat;
import com.chatter.chatter.dto.Message;
import com.chatter.chatter.dto.MessageDto;
import com.chatter.chatter.service.ChatService;
import com.chatter.chatter.service.MessageService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;

import com.chatter.chatter.dto.ChatUserStatus;

@Controller
public class ChatWebSocketController {

    // Store online users by chat ID
    private final ConcurrentMap<Integer, ConcurrentMap<String, Boolean>> usersStatusPerChat = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentMap<String, Long> lastHeartbeatTimestamps = new ConcurrentHashMap<>();
    private final ChatService chatService;
    private final MessageService messageService; // Inject MessageService

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, ChatService chatService, MessageService messageService) {
        this.messagingTemplate = messagingTemplate;
        this.chatService = chatService;
        this.messageService = messageService;
    }


    public ConcurrentMap<String, Boolean> getAllUsersStatus(int chatId) {
        usersStatusPerChat.putIfAbsent(chatId, new ConcurrentHashMap<>());
        ConcurrentMap<String, Boolean> usersStatus = usersStatusPerChat.get(chatId);

        Set<String> allUsers = chatService.getAllUsersInChat(chatId); // Hypothetical method

        for (String user : allUsers) {
            usersStatus.putIfAbsent(user, false); // Default to false (offline)
        }

        return usersStatus;
    }

    @MessageMapping("/chat/online")
    @SendTo("/topic/online")
    public ConcurrentMap<String, Boolean> markUserOnline(ChatUserStatus status) {
        getAllUsersStatus(status.getChatId());
        usersStatusPerChat.get(status.getChatId()).put(status.getUsername(), true); // Mark user as online
        messagingTemplate.convertAndSend("/topic/chat/" + status.getChatId() + "/online", usersStatusPerChat.get(status.getChatId()));
        return usersStatusPerChat.get(status.getChatId());
    }


    @MessageMapping("/chat/offline")
    @SendTo("/topic/online")
    public ConcurrentMap<String, Boolean> markUserOffline(ChatUserStatus status) {
        usersStatusPerChat.putIfAbsent(status.getChatId(), new ConcurrentHashMap<>());
        usersStatusPerChat.get(status.getChatId()).put(status.getUsername(), false); // Mark user as offline
        messagingTemplate.convertAndSend("/topic/chat/" + status.getChatId() + "/online", usersStatusPerChat.get(status.getChatId()));
        return usersStatusPerChat.get(status.getChatId());
    }


    @MessageMapping("/chat/message")
    public void handleMessage(MessageDto message) {
        int chatId = message.getChatId();
        String content = message.getMessage();
        String author = message.getAuthor();
        Chat chat = chatService.getChatById(chatId);
        Message messagedb = new Message(chat, content, author);
        messageService.saveMessage(messagedb);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/message", message);
    }


    @MessageMapping("/chat/heartbeat")
    public void handleHeartbeat(ChatUserStatus status) {
        String username = status.getUsername();
        int chatId = status.getChatId();
        lastHeartbeatTimestamps.put(username + "-" + chatId, System.currentTimeMillis());
    }

    @Scheduled(fixedRate = 30000)
    public void detectOfflineUsers() {
        long now = System.currentTimeMillis();
        long cutoff = 30000;

        lastHeartbeatTimestamps.forEach((key, timestamp) -> {
            if (now - timestamp > cutoff) {
                String[] parts = key.split("-");
                String username = parts[0];
                Integer chatId = Integer.valueOf(parts[1]);

                if (usersStatusPerChat.containsKey(chatId)) {
                    usersStatusPerChat.get(chatId).remove(username);
                    messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/online", usersStatusPerChat.get(chatId));
                    lastHeartbeatTimestamps.remove(key);
                }

            }
        });
    }

}