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

    public Page<Log> getAllLogs(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return logRepository.findAll(pageable);
    }

    public Page<Log> searchLogs(String author, String action, String target, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        return logRepository.findByAuthorContainingAndActionContainingAndTargetContaining(author, action, target, pageable);
    }

}