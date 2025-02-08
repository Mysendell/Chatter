package com.chatter.chatter.dao;

import com.chatter.chatter.dto.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    Page<Chat> findByUsers_Username(String username, Pageable pageable);

    boolean existsByIdAndUsers_Username(int chatId, String username);
}