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

/**
 * Service handling operations related to chats.
 * Responsible for creating, retrieving, updating, and validating chats and their users.
 */
@Service
public class ChatService {

    private final ChatRepository chatRepository;

    @Autowired
    public ChatService(ChatRepository chatRepository) {
        this.chatRepository = chatRepository;
    }

    /**
     * Retrieves a paginated list of chats associated with the specified user.
     *
     * @param username the username of the user whose chats are to be fetched
     * @param page the index of the page to be retrieved (0-based)
     * @param size the number of chats per page
     * @return a paginated list of chats associated with the specified user
     */
    public Page<Chat> getUserChats(String username, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return chatRepository.findByUsers_Username(username, pageable);
    }

    /**
     * Retrieves a chat based on the provided chat ID.
     *
     * @param id the unique identifier of the chat to be retrieved
     * @return the Chat corresponding to the provided ID
     * @throws IllegalArgumentException if the chat with the specified ID does not exist
     */
    public Chat getChatById(int id) {
        return chatRepository.findById(id).orElseThrow(() ->
                new IllegalArgumentException("Chat not found with id: " + id));
    }

    /**
     * Saves the provided chat object to the repository.
     *
     * @param chat the chat object to be saved
     * @return the saved chat object with updated information from the repository
     */
    public Chat saveChat(Chat chat) {
        return chatRepository.save(chat);
    }

    /**
     * Validates if a given user is a participant of a specific chat.
     * Throws a SecurityException if the user is not part of the chat.
     *
     * @param username the username of the user to be checked in the chat
     * @param chatId the unique identifier of the chat to be checked
     * @throws SecurityException if the user is not part of the specified chat
     */
    public void userInChat(String username, int chatId) {
        if(!chatRepository.existsByIdAndUsers_Username(chatId, username))
            throw new SecurityException("User not in chat");
    }

    /**
     * Creates a new chat with the specified users and name.
     *
     * @param users the set of users to be added to the chat
     * @param name the name of the chat
     * @return the unique identifier of the created chat
     */
    public int createChat(Set<User> users, String name) {
        Chat chat = new Chat();
        chat.setName(name);
        chat.setUsers(users);
        chatRepository.save(chat);
        return chat.getId();
    }

    /**
     * Retrieves a chat that the specified user is authorized to access.
     * Validates if the user is part of the chat before returning the chat details.
     *
     * @param username the username of the user requesting access to the chat
     * @param chatId the unique identifier of the chat to be retrieved
     * @return the Chat object corresponding to the provided chat ID
     * @throws SecurityException if the user is not part of the specified chat
     * @throws IllegalArgumentException if the chat with the specified ID does not exist
     */
    public Chat getAuthorizedChat(String username, int chatId) {
        userInChat(username, chatId);
        return getChatById(chatId);
    }

    /**
     * Searches for chats based on a search term and a list of usernames.
     *
     * @param page the page number to retrieve, starting from 0
     * @param size the number of records per page
     * @param chat the search term to filter chats by name
     * @param users the list of usernames for filtering chats by their participants
     * @return a paginated list of chats matching the search criteria
     */
    public Page<Chat> searchChats(int page, int size, String chat, List<String> users) {
        Pageable pageable = PageRequest.of(page, size);
        long usernamesSize = users.size();
        return chatRepository.findByNameContainingAndUsers_UsernameIntersection(chat, users, usernamesSize, pageable);
    }

    /**
     * Removes a user from the chat with the specified chat ID.
     * If the user list becomes empty after removal, the chat is deleted.
     *
     * @param chatId The unique identifier of the chat.
     * @param username The username of the user to be removed from the chat.
     */
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

    /**
     * Adds a user to a specified chat based on the chatId.
     *
     * @param chatId the unique identifier of the chat to which the user will be added
     * @param username the username of the user to be added to the chat
     */
    public void addUser(int chatId, String username){
        Chat chat = getChatById(chatId);
        HashSet<User> users = new HashSet<>(chat.getUsers());
        users.add(new User(username));
        chat.setUsers(new HashSet<>(users));
        chatRepository.save(chat);
    }

    /**
     * Retrieves a set of usernames of all users participating in a given chat.
     *
     * @param chatId the unique identifier of the chat to retrieve users from
     * @return a set of usernames of all users in the specified chat
     */
    @Transactional
    public Set<String> getAllUsersInChat(int chatId) {
        Chat chat = getChatById(chatId);
        return chat.getUsers().stream().map(User::getUsername).collect(java.util.stream.Collectors.toSet());
    }

}