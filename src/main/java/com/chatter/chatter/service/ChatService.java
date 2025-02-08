package com.chatter.chatter.service;

import com.chatter.chatter.dao.ChatRepository;
import com.chatter.chatter.dto.Chat;
import com.chatter.chatter.dto.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Page<Chat> getUserChats(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatRepository.findByUsers_Username(username, pageable);
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

    public Page<Chat> searchChats(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatRepository.findByUsers_UsernameContaining(username, pageable);
    }

    public void removeUser(int chatId, String username){
        Chat chat = getChatById(chatId);
        HashSet<User> users = new HashSet<>(chat.getUsers());
        users.removeIf(user -> user.getUsername().equals(username));
        if(chat.getUsers().isEmpty())
            chatRepository.deleteById(chatId);
        else
            chat.setUsers(new HashSet<>(users));
        chatRepository.save(chat);
    }

    public void addUser(int chatId, String username){
        Chat chat = getChatById(chatId);
        HashSet<User> users = new HashSet<>(chat.getUsers());
        users.add(new User(username));
        chat.setUsers(new HashSet<>(users));
        chatRepository.save(chat);
    }

    @Transactional
    public Set<String> getAllUsersInChat(int chatId) {
        Chat chat = getChatById(chatId);
        return chat.getUsers().stream().map(User::getUsername).collect(java.util.stream.Collectors.toSet());
    }

}