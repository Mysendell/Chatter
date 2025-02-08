package com.chatter.chatter.dto;

import java.time.Instant;

public class MessageDto {
    private int chatId;
    private String message;
    private String author;
    private Instant timestamp;

    public int getChatId() {
        return chatId;
    }

    public void setChatId(int chatId) {
        this.chatId = chatId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
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

    public void setTimestamp(Object timestamp) {
        if (timestamp instanceof Long) {
            this.timestamp = Instant.ofEpochMilli((Long) timestamp);
        } else if (timestamp instanceof Instant) {
            this.timestamp = (Instant) timestamp;
        } else {
            throw new IllegalArgumentException("Unsupported type for timestamp: " + timestamp);
        }
    }

}
