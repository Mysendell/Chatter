package com.chatter.chatter.service;

import com.chatter.chatter.dao.LogRepository;
import com.chatter.chatter.dao.UserRepository;
import com.chatter.chatter.dto.Log;
import com.chatter.chatter.dto.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final HttpSession session;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, LogRepository logRepository, HttpSession session) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.logRepository = logRepository;
        this.session = session;
    }

    public void Register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isEmpty()) {
            user.setAuth("USER");
            user.setPassword(passwordEncoder.encode(user.getPassword()));
            userRepository.save(user);
            Log log = new Log(user.getUsername(), "Register");
            logRepository.save(log);
        }
        login(user);
    }

    public void login(User user) {
        User foundUser = userRepository.findByUsername(user.getUsername()).orElse(null);
        if (foundUser != null) {
            foundUser.setPassword(passwordEncoder.encode(user.getPassword()));
            if (passwordEncoder.matches(user.getPassword(), foundUser.getPassword())) {
                System.out.println("Login successful");
                session.setAttribute("loggedInUser", user.getUsername());
                Log log = new Log(user.getUsername(), "Login");
                logRepository.save(log);
                System.out.println("Welcome " + user.getUsername());
                return;
            }
        }
            System.out.println("Login failed");
    }


    public void logout() {
        String username = (String) session.getAttribute("loggedInUser");
        if (username != null) {
            session.removeAttribute("loggedInUser");
            Log log = new Log(username, "Logout");
            logRepository.save(log);
            System.out.println("Goodbye " + username);
        } else {
            System.out.println("No user logged in");
        }
    }

    public boolean checkAuthority(String authority) {
        String username = (String) session.getAttribute("loggedInUser");
        if (username != null) {
            User user = userRepository.findByUsername(username).get();
            return user.getAuth().equals(authority);
        }
        return false;
    }
}