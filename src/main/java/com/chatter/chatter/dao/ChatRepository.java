package com.chatter.chatter.dao;

import com.chatter.chatter.dto.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ChatRepository extends JpaRepository<Chat, String> {

    @Query(value = "SELECT * FROM Chat c WHERE JSON_CONTAINS(c.users, :username, '$')", nativeQuery = true)
    List<Chat> findByUsername(@Param("username") String username);

}