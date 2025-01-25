package com.chatter.chatter.dao;

import com.chatter.chatter.dto.Log;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;

public interface LogRepository extends JpaRepository<Log, Instant> {
}
