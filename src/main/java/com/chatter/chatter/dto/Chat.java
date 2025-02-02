package com.chatter.chatter.dto;

import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;

@Entity
public class Chat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String name;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
            name = "chat_users",
            joinColumns = @JoinColumn(name = "chat_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "chat", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("timestamp ASC")
    private Set<Message> messages = new HashSet<>();


    public Chat() {
    }

    public Chat(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Set<User> getUsers() {
        return users;
    }

    public String getUsersString() {
        StringBuilder sb = new StringBuilder();
        for (User user : users) {
            if (!sb.isEmpty()) {
                sb.append(", ");
            }
            sb.append(user.getUsername());
        }
        return sb.toString();
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    public void addUser(User user) {
        this.users.add(user);
        user.getChats().add(this);
    }

    public void removeUser(User user) {
        this.users.remove(user);
        user.getChats().remove(this);
    }

    public Set<Message> getMessages() {
        return messages;
    }

    public void addMessage(String content, String author) {
        Message message = new Message(this, content, author);
        this.messages.add(message);

    }
}