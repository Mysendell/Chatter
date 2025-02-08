package com.chatter.chatter.dto;

public class ChatDto {
    private int id;
    private String name;
    private String usersString;

    public ChatDto(Chat chat) {
        this.id = chat.getId();
        this.name = chat.getName();
        this.usersString = chat.getUsersString();
    }

    public String getUsersString() {
        return usersString;
    }

    public void setUsersString(String usersString) {
        this.usersString = usersString;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
