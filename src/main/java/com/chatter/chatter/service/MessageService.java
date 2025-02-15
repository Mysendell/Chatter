package com.chatter.chatter.service;

import com.chatter.chatter.dao.MessageRepository;
import com.chatter.chatter.dto.Message;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Service class responsible for handling operations related to messages.
 * Provides methods to save messages to the repository.
 */
@Service
public class MessageService {

    private final MessageRepository messageRepository;

    @Autowired
    public MessageService(MessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    /**
     * Saves a message to the repository
     * @param message The message to be saved
     */
    public void saveMessage(Message message) {
        messageRepository.save(message);
    }
}