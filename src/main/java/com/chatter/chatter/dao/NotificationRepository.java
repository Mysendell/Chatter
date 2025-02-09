package com.chatter.chatter.dao;

import com.chatter.chatter.dto.Chat;
import com.chatter.chatter.dto.Notification;
import com.chatter.chatter.dto.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface NotificationRepository extends JpaRepository<Notification, Integer> {
    List<Notification> findByUserAndChatAndSeenFalse(User user, Chat chat);
}