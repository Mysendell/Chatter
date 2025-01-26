package com.chatter.chatter;

import com.chatter.chatter.dao.LogRepository;
import com.chatter.chatter.dto.User;
import com.chatter.chatter.service.UserService;
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
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public WebController(UserService userService, PasswordEncoder passwordEncoder, LogRepository logRepository) {
        this.userService = userService;
        this.logRepository = logRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/login")
    public String handleLogin(@ModelAttribute User user) {
        String hashedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(hashedPassword);
        userService.saveUser(user);
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
    public String home() {
        return "home";
    }

    @GetMapping("/logout")
    public String logout() {
        userService.logout();
        return "redirect:/";
    }

    @GetMapping("/logs")
    public String logs(Model model) {
        String username = (String) model.getAttribute("loggedInUser");
        if (username == null) {
            System.out.println("No user logged in");
            return "redirect:/login";
        }
        if(!userService.checkAuthority("Admin")) {
            System.out.println("User does not have permission to view logs");
            return "redirect:/home";
        }
        model.addAttribute("logs", logRepository.findAll());
        return "logs";
    }
}
