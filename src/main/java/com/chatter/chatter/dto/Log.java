package com.chatter.chatter.dto;

import jakarta.persistence.*;

import java.time.Instant;

@Entity
public class Log {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Automatically generates IDs
    private Long id;
    private final Instant timestamp = Instant.now();
    private String author;
    private String target;
    private String action;
    private int actionId;

    public Log(String author, String action, int actionId) {
        this.author = author;
        this.target = author;
        this.action = action;
        this.actionId = actionId;
    }

    public Log(String author, String target,String action, int actionId) {
        this.author = author;
        this.target = target;
        this.action = action;
        this.actionId = actionId;
    }

    public Log() {

    }

    public String getAuthor() {
        return author;
    }

    public String getAction() {
        return action;
    }

    public int getactionId() {
        return actionId;
    }

    public Instant getTimestamp() {
        return timestamp;
    }

    public String getTarget() {
        return target;
    }

    public long getId() {
        return id;
    }
}
