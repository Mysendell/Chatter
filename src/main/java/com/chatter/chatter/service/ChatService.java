package com.chatter.chatter.service;

import com.chatter.chatter.dao.ChatRepository;
import com.chatter.chatter.dto.Chat;
import com.chatter.chatter.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class ChatService {

    private final ChatRepository chatRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    public Set<Chat> getUserChats(String username) {
        List<Chat> chats = chatRepository.findByUsers_Username(username);
        return new HashSet<>(chats);
    }

    public Chat getChatById(int id) {
        return chatRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Chat not found with id: " + id));
    }

    public Chat saveChat(Chat chat) {
        return chatRepository.save(chat);
    }

    public void userInChat(String username, int chatId) {
        if(!chatRepository.existsByIdAndUsers_Username(chatId, username))
            throw new SecurityException("User not in chat");
    }

    public int createChat(Set<User> users, String name) {
        Chat chat = new Chat();
        chat.setName(name);
        chat.setUsers(users);
        chatRepository.save(chat);
        return chat.getId();
    }

    public Chat getAuthorizedChat(String username, int chatId) {
        userInChat(username, chatId);
        return getChatById(chatId);
    }
}