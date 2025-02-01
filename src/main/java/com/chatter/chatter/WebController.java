package com.chatter.chatter;

import com.chatter.chatter.dao.ChatRepository;
import com.chatter.chatter.dao.LogRepository;
import com.chatter.chatter.dto.Chat;
import com.chatter.chatter.dto.User;
import com.chatter.chatter.service.UserService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller

public class WebController {

    private final UserService userService;
    private final LogRepository logRepository;
    private final HttpSession session;
    private final ChatRepository chatRepository;

    @Autowired
    public WebController(HttpSession session, UserService userService, ChatRepository chatRepository , LogRepository logRepository) {
        this.session = session;
        this.userService = userService;
        this.logRepository = logRepository;
        this.chatRepository = chatRepository;
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute User user) {
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
    public String home(Model model) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) {
            return "redirect:/login";
        }
        model.addAttribute("username", username);
        model.addAttribute("chats", chatRepository.findByUsername(username));
        return "home";
    }

    @GetMapping("/chat")
    public String chat() {
        return "chat";
    }

    @PostMapping("/chat")
    public String createChat(@ModelAttribute Chat chat) {
        System.out.println(chat.getUsers());
        chatRepository.save(chat);
        return chat();
    }

    @GetMapping("/logout")
    public String logout() {
        userService.logout();
        return "redirect:/";
    }

    @GetMapping("/logs")
    public String logs(Model model) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username == null) {
            return "redirect:/login";
        }
        if(!userService.checkAuthority("ADMIN")) {
            return "redirect:/home";
        }
        model.addAttribute("logs", logRepository.findAll());
        return "logs";
    }
}
