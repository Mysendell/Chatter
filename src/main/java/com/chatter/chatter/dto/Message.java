package com.chatter.chatter.dto;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @ManyToOne
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    private String content;
    private String author;
    private Instant timestamp;

    public Message() {}

    public Message(Chat chat, String content, String author) {
        this.chat = chat;
        this.content = content;
        this.author = author;
        this.timestamp = Instant.now();
    }

    public int getId() {
        return id;
    }

    public Chat getChat() {
        return chat;
    }

    public void setChat(Chat chat) {
        this.chat = chat;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Instant timestamp) {
        this.timestamp = timestamp;
    }
}