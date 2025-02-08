package com.chatter.chatter.controller;

import com.chatter.chatter.dto.Chat;
import com.chatter.chatter.dto.ChatDto;
import com.chatter.chatter.dto.Log;
import com.chatter.chatter.dto.User;
import com.chatter.chatter.service.ChatService;
import com.chatter.chatter.service.LogService;
import com.chatter.chatter.service.SessionService;
import com.chatter.chatter.service.UserService;
import jakarta.persistence.PreUpdate;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller

public class WebController {

    private final UserService userService;
    private final SessionService sessionService;
    private final ChatService chatService;
    private final LogService logService;

    @Autowired
    public WebController(LogService logService,SessionService sessionService, UserService userService, ChatService chatService) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.chatService = chatService;
        this.logService = logService;
    }

    @PostMapping("/login")
    public String handleLogin(@Valid @ModelAttribute User user, BindingResult result) {
        if (result.hasErrors()) {
            return "login";
        }
        userService.Register(user);
        return "redirect:/home";
    }

    @GetMapping("/login")
    public String login() {
        if(sessionService.isLoggedIn())
            return "redirect:/home";
        return "login";
    }

    @GetMapping("")
    public String index() {
        return "index";
    }

    @GetMapping("/home")
    public String home() {
        String username = sessionService.getLoggedInUser();
        sessionService.requireLoggedInUser();
        return "home";
    }


    @GetMapping("/chat")
    public String chat(Model model, @RequestParam(value = "id", required = false) Integer id) {
        requireNonNull(id, "Chat id must be provided");
        sessionService.requireLoggedInUser();
        String username = sessionService.getLoggedInUser();
        Chat chat = chatService.getAuthorizedChat(username, id);
        model.addAttribute("messages", chat.getMessages());
        return "chat";
    }

    @PostMapping("/chat")
    public String createChat(@RequestParam("name") String name, @RequestParam("userString") String usersString) {
        Set<User> users = userService.getUsersFromString(usersString);
        String username = sessionService.getLoggedInUser();
        users.add(userService.getUserByUsername(username));
        int id = chatService.createChat(users, name);
        logService.saveLog(username, usersString,"Create chat");
        return "redirect:/chat?id=" + id;
    }


    @GetMapping("/logout")
    public String logout() {
        userService.logout();
        return "redirect:/";
    }

    @GetMapping("/logs")
    public String logs() {
        userService.authorizeAdminAccess();
        return "logs";
    }

    @GetMapping("/api/current-user")
    @ResponseBody
    public String getCurrentUser() {
        return sessionService.getLoggedInUser();
    }


    public void requireNonNull(Object param, String errorMessage) {
        if (param == null) {
            throw new IllegalArgumentException(errorMessage);
        }
    }

    @GetMapping("/api/logs")
    @ResponseBody
    public Page<Log> getLogs(
            @RequestParam(defaultValue = "") String action,
            @RequestParam(defaultValue = "") String target,
            @RequestParam(defaultValue = "") String author,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "15") int size
    ) {
        return logService.searchLogs(author, action, target, page, size);
    }

    @GetMapping("/api/chats")
    @ResponseBody
    public Page<ChatDto> searchChats(
            @RequestParam(defaultValue = "") String username,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        Page<Chat> chats = chatService.searchChats(username, page, size);
        return chats.map(ChatDto::new);
    }

    @GetMapping("/api/leave-chat")
    public void leaveChat(@RequestParam int chatId, @RequestParam String username) {
        chatService.userInChat(username, chatId);
        chatService.removeUser(chatId, username);
    }

    @GetMapping("/api/add-user")
    public void addUser(
            @RequestParam int chatId, @RequestParam String username, @RequestParam String author
    ) {
        chatService.userInChat(author, chatId);
        chatService.addUser(chatId, username);
    }

    @GetMapping("/api/remove-user")
    public void removeUser(
            @RequestParam int chatId, @RequestParam String username, @RequestParam String author
    ) {
        chatService.userInChat(author, chatId);
        chatService.removeUser(chatId, username);
    }
}
