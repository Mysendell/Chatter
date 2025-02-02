package com.chatter.chatter.dao;

import com.chatter.chatter.dto.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    List<Chat> findByUsers_Username(String username);

    boolean existsByIdAndUsers_Username(int chatId, String username);
}