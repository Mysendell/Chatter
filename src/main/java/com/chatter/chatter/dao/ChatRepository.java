package com.chatter.chatter.dao;

import com.chatter.chatter.dto.Chat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Integer> {
    Page<Chat> findByUsers_Username(String username, Pageable pageable);

    boolean existsByIdAndUsers_Username(int chatId, String username);

    @Query("SELECT c FROM Chat c " +
            "JOIN c.users u " +
            "WHERE c.name LIKE %:name% " +
            "AND u.username IN :usernames " +
            "GROUP BY c " +
            "HAVING COUNT(u.username) = :usernamesSize")
    Page<Chat> findByNameContainingAndUsers_UsernameIntersection(
            @Param("name") String name,
            @Param("usernames") List<String> usernames,
            @Param("usernamesSize") long usernamesSize,
            Pageable pageable);

}