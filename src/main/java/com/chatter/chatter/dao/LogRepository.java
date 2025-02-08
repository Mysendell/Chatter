package com.chatter.chatter.dao;

import com.chatter.chatter.dto.Log;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;

public interface LogRepository extends JpaRepository<Log, Instant> {
    Page<Log> findAll(Pageable pageable);
}
