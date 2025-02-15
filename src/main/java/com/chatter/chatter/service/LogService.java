package com.chatter.chatter.service;

import com.chatter.chatter.dao.LogRepository;
import com.chatter.chatter.dto.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service class responsible for handling operations related to logs.
 * Provides methods to save logs, retrieve all logs, and searching logs with filters.
 */
@Service
public class LogService {

    private final LogRepository logRepository;

    @Autowired
    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    /**
     * Saves a log to the repository
     * @param username The user responsible for the action
     * @param action The action committed by the user
     */
    public void saveLog(String username, String action) {
        Log log = new Log(username, action);
        logRepository.save(log);
    }

    /**
     * Saves a log to the repository
     * @param username The user responsible for the action
     * @param target The user affected by the action
     * @param action The action committed by the user
     */
    public void saveLog(String username, String target, String action) {
        Log log = new Log(username, target, action);
        logRepository.save(log);
    }

    /**
     * Retrieves a page of logs
     * @param page The page being retrieved
     * @param size How many records there are in the page
     * @return A page containing logs according to the parameters
     */
    public Page<Log> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return logRepository.findAll(pageable);
    }

    /**
     * Searches and retrieves a page of logs according to the parameters
     * @param author The author of the logs being searched
     * @param action The action commited by the author
     * @param target The target of the action
     * @param page Which page should be retrieved
     * @param size How many logs there are in the page
     * @return A page containing logs according
     */
    public Page<Log> searchLogs(String author, String action, String target, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return logRepository.findByAuthorContainingAndActionContainingAndTargetContaining(author, action, target, pageable);
    }

}