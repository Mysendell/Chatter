package com.chatter.chatter.service;

import com.chatter.chatter.dao.NotificationRepository;
import com.chatter.chatter.dao.UserRepository;
import com.chatter.chatter.dto.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final ChatService chatService;

    @Autowired
    public NotificationService(SimpMessagingTemplate messagingTemplate, NotificationRepository notificationRepository, UserRepository userRepository, UserService userService, ChatService chatService) {
        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.messagingTemplate = messagingTemplate;
        this.userService = userService;
        this.chatService = chatService;
    }

    @Transactional
    public void createNotificationsForChat(Message message, Chat chat) {
        Set<User> users = chat.getUsers();

        List<User> allUsers = userRepository.findAll();
        List<User> offlineUsers = allUsers.stream()
                .filter(user -> !users.contains(user))
                .toList();

        for (User user : offlineUsers) {
            Notification notification = new Notification(user, chat, message);
            notificationRepository.save(notification);
            messagingTemplate.convertAndSend(
                    "/topic/user/" + user.getUsername() + "/notifications",
                    new NotificationDto(chat.getId(), "New message in " + chat.getName())
            );

        }
    }

    public List<NotificationDto> getUnreadNotifications(String username, int chatId) {
        User user = userService.getUserByUsername(username);
        Chat chat = chatService.getChatById(chatId);
        return notificationRepository.findByUserAndChatAndSeenFalse(user, chat)
                .stream()
                .map(notification -> new NotificationDto(
                        notification.getChat().getId(),
                        notification.getMessage().getContent()

                ))
                .collect(Collectors.toList());

    }

    public void markNotificationsAsSeen(String username, int chatId) {
        User user = userService.getUserByUsername(username);
        Chat chat = chatService.getChatById(chatId);
        List<Notification> notifications = notificationRepository.findByUserAndChatAndSeenFalse(user, chat);
        notificationRepository.deleteAll(notifications);
    }
}