package com.chatter.chatter.controller;

import com.chatter.chatter.dto.*;
import com.chatter.chatter.service.*;
import jakarta.persistence.PreUpdate;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

@Controller

public class WebController {

    private final UserService userService;
    private final SessionService sessionService;
    private final ChatService chatService;
    private final LogService logService;
    private final NotificationService notificationService;
    private final ChatWebSocketController chatWebSocketController;

    @Autowired
    public WebController(
            LogService logService, SessionService sessionService,
            UserService userService, ChatService chatService,
            NotificationService notificationService, ChatWebSocketController chatWebSocketController) {
        this.userService = userService;
        this.sessionService = sessionService;
        this.chatService = chatService;
        this.logService = logService;
        this.notificationService = notificationService;
        this.chatWebSocketController = chatWebSocketController;
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
        if(users.contains(userService.getUserByUsername(username)))
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
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam(defaultValue = "") String chat,
            @RequestParam(defaultValue = "") List<String> userList
    ) {
        Page<Chat> chats = chatService.searchChats(page, size, chat, userList);
        return chats.map(ChatDto::new);
    }

    @GetMapping("/api/leave-chat")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void leaveChat(@RequestParam int chatId, @RequestParam String username) {
        chatService.userInChat(username, chatId);
        chatService.removeUser(chatId, username);
        logService.saveLog(username,"Left chat: " + chatId);
        chatWebSocketController.removeUser(chatId, username);
    }

    @GetMapping("/api/add-user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void addUser(
            @RequestParam int chatId, @RequestParam String username, @RequestParam String author
    ) {
        chatService.userInChat(author, chatId);
        chatService.addUser(chatId, username);
        logService.saveLog(author, username,"Add user");
    }

    @GetMapping("/api/remove-user")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeUser(
            @RequestParam int chatId, @RequestParam String username, @RequestParam String author
    ) {
        chatService.userInChat(author, chatId);
        chatService.removeUser(chatId, username);
        logService.saveLog(author, username,"Remove user");
        chatWebSocketController.removeUser(chatId, username);
    }

    @GetMapping("/api/mark-notifications-seen")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void markNotificationsAsSeen(@RequestParam int chatId, @RequestParam String username) {
        notificationService.markNotificationsAsSeen(username, chatId);
    }

    @GetMapping("api/notifications")
    @ResponseBody
    public List<NotificationDto> getNotifications(@RequestParam String username, @RequestParam int chatId) {
        return notificationService.getUnreadNotifications(username, chatId);
    }
}
