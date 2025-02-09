package com.chatter.chatter.dto;

public class NotificationDto {
    int chatId;
    String message;

    public NotificationDto(int chatId, String message) {
        this.chatId = chatId;
        this.message = message;
    }

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
}
