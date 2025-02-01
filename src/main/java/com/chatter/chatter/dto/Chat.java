package com.chatter.chatter.dto;

import com.chatter.chatter.service.ChatService;
import jakarta.persistence.*;
import java.util.List;
import java.util.Arrays;
import java.util.stream.Collectors;

@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String usersString;
    private String name;
    private String messages;

    @Convert(converter = ChatService.class)
    private List<String> users;

    public Chat() {
    }

    public Chat(String usersString, String name, String messages) {
        this.usersString = usersString;
        this.name = name;
        this.messages = messages;
        this.users = convertToUserArray();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUsersString() {
        return usersString;
    }

    public void setUsersString(String usersString) {
        this.usersString = usersString;
        this.users = convertToUserArray();
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<String> getUsers() {
        return users;
    }

    public void setUsers(List<String> users) {
        this.users = users;
    }

    @PrePersist
    @PreUpdate
    private void preSave() {
        this.users = convertToUserArray();
    }

    private List<String> convertToUserArray() {
        return Arrays.stream(usersString.split(","))
                .map(String::trim)
                .collect(Collectors.toList());
    }
}