package com.chatter.chatter.controller;

import com.chatter.chatter.dto.Chat;
import com.chatter.chatter.dto.User;
import com.chatter.chatter.service.ChatService;
import com.chatter.chatter.service.LogService;
import com.chatter.chatter.service.SessionService;
import com.chatter.chatter.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
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
        return "login";
    }

    @GetMapping("")
    public String index() {
        return "index";
    }

    @GetMapping("/home")
    public String home(
            Model model,
            @RequestParam(defaultValue = "0") int page, // Default to the first page
            @RequestParam(defaultValue = "10") int size // Default size
    ) {
        String username = sessionService.getLoggedInUser();
        sessionService.requireLoggedInUser();
        model.addAttribute("chats", chatService.getUserChats(username, page, size));
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
    public String logs(
            Model model,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        userService.authorizeAdminAccess();
        model.addAttribute("logs", logService.getAllLogs(page, size));
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
}
