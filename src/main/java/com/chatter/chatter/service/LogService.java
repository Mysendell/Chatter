package com.chatter.chatter.service;

import com.chatter.chatter.dao.LogRepository;
import com.chatter.chatter.dto.Log;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class LogService {

    private final LogRepository logRepository;

    @Autowired
    public LogService(LogRepository logRepository) {
        this.logRepository = logRepository;
    }

    public void saveLog(String username, String action) {
        Log log = new Log(username, action);
        logRepository.save(log);
    }
    public void saveLog(String username, String target, String action) {
        Log log = new Log(username, target, action);
        logRepository.save(log);
    }

    public List<Log> getAllLogs() {
        return logRepository.findAll();
    }
}