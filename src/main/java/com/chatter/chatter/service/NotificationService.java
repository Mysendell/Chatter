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

/**
 * Service responsible for managing notifications in the system.
 * This service handles the creation, fetching, and marking of notifications related to user activity in chats.
 */
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

    /**
     * Creates a new notification for all offline users in a chat
     * @param message The message triggering the notification
     * @param chat The chat the notification happened in
     */
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

    /**
     * Gets all unread notification from a chat
     * @param username The user trying to retrieve the notifications
     * @param chatId The chat they're trying to get the notifications from
     * @return A list containing all notifications in that chat
     */
    public List<NotificationDto> getUnreadNotifications(String username, int chatId) {
        User user = userService.getUserByUsername(username);
        Chat chat = chatService.getChatById(chatId);
        chatService.userInChat(username, chatId);
        return notificationRepository.findByUserAndChatAndSeenFalse(user, chat)
                .stream()
                .map(notification -> new NotificationDto(
                        notification.getChat().getId(),
                        notification.getMessage().getContent()

                ))
                .collect(Collectors.toList());

    }

    /**
     * Mark all notification in a chat as seen by a certain user
     * @param username The user who opened the chat and saw the notifications
     * @param chatId The chat the user saw the notifications in
     */
    public void markNotificationsAsSeen(String username, int chatId) {
        User user = userService.getUserByUsername(username);
        Chat chat = chatService.getChatById(chatId);
        List<Notification> notifications = notificationRepository.findByUserAndChatAndSeenFalse(user, chat);
        notificationRepository.deleteAll(notifications);
    }
}