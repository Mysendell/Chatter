package com.chatter.chatter.controller;

import com.chatter.chatter.dto.Chat;
import com.chatter.chatter.dto.Message;
import com.chatter.chatter.dto.MessageDto;
import com.chatter.chatter.service.ChatService;
import com.chatter.chatter.service.MessageService;
import com.chatter.chatter.service.NotificationService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Controller;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.Set;

import com.chatter.chatter.dto.ChatUserStatus;

@Controller
public class ChatWebSocketController {


    private final ConcurrentMap<Integer, ConcurrentMap<String, Boolean>> usersStatus = new ConcurrentHashMap<>();
    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentMap<String, Long> lastHeartbeatTimestamps = new ConcurrentHashMap<>();
    private final ChatService chatService;
    private final MessageService messageService;
    private final NotificationService notificationService;

    public ChatWebSocketController(SimpMessagingTemplate messagingTemplate, ChatService chatService, MessageService messageService, NotificationService notificationService) {
        this.chatService = chatService;
        this.messageService = messageService;
        this.notificationService = notificationService;
        this.messagingTemplate = messagingTemplate;
    }



    public void getAllUsersStatus(int chatId) {
        if (!usersStatus.containsKey(chatId)) {
            usersStatus.putIfAbsent(chatId, new ConcurrentHashMap<>());
        }

        ConcurrentMap<String, Boolean> usersStatus = this.usersStatus.get(chatId);
        Set<String> allUsers;
        if (chatId != 0) {
            allUsers = chatService.getAllUsersInChat(chatId);
            for (String user : allUsers) {
                usersStatus.putIfAbsent(user, false);
            }
        }
    }


    @MessageMapping("/chat/online")
    @SendTo("/topic/online")
    public ConcurrentMap<Integer, ConcurrentMap<String, Boolean>> markUserOnline(ChatUserStatus status) {
        getAllUsersStatus(status.getChatId());

        usersStatus.get(status.getChatId()).put(status.getUsername(), true);

        messagingTemplate.convertAndSend(
                "/topic/chat/" + status.getChatId() + "/online",
                usersStatus.get(status.getChatId())
        );

        return usersStatus;
    }



    @MessageMapping("/chat/offline")
    @SendTo("/topic/online")
    public ConcurrentMap<Integer, ConcurrentMap<String, Boolean>> markUserOffline(ChatUserStatus status) {
        getAllUsersStatus(status.getChatId());

        if(status.getChatId() != 0)
            usersStatus.get(status.getChatId()).put(status.getUsername(), false);
        else
            usersStatus.get(status.getChatId()).remove(status.getUsername());
        messagingTemplate.convertAndSend(
                "/topic/chat/" + status.getChatId() + "/online",
                usersStatus.get(status.getChatId())
        );

        return usersStatus;
    }



    @MessageMapping("/chat/message")
    public void handleMessage(MessageDto message) {
        int chatId = message.getChatId();
        String content = message.getMessage();
        String author = message.getAuthor();
        Chat chat = chatService.getChatById(chatId);
        Message messagedb = new Message(chat, content, author);
        messageService.saveMessage(messagedb);

        notificationService.createNotificationsForChat(messagedb, chat);

        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/message", message);
    }


    @MessageMapping("/chat/heartbeat")
    public void handleHeartbeat(ChatUserStatus status) {
        String username = status.getUsername();
        int chatId = status.getChatId();
        lastHeartbeatTimestamps.put(username + "-" + chatId, System.currentTimeMillis());
    }

    @Scheduled(fixedRate = 1000)
    public void detectOfflineUsers() {
        long now = System.currentTimeMillis();
        long cutoff = 1000;

        lastHeartbeatTimestamps.forEach((key, timestamp) -> {
            if (now - timestamp > cutoff) {
                String[] parts = key.split("-");
                String username = parts[0];
                Integer chatId = Integer.valueOf(parts[1]);

                if (usersStatus.containsKey(chatId)) {
                    if(chatId != 0)
                        usersStatus.get(chatId).put(username, false);
                    else
                        usersStatus.get(chatId).remove(username);
                    messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/online", usersStatus.get(chatId));
                    messagingTemplate.convertAndSend("/topic/online", usersStatus);
                    lastHeartbeatTimestamps.remove(key);
                }

            }
        });
    }
    
    public void removeUser(int chatId, String username){
        getAllUsersStatus(chatId);
        usersStatus.get(chatId).remove(username);
        messagingTemplate.convertAndSend("/topic/chat/" + chatId + "/online", usersStatus.get(chatId));
        messagingTemplate.convertAndSend("/topic/chat/" + username + "/removed", "");
        messagingTemplate.convertAndSend("/topic/online", usersStatus);
    }

}