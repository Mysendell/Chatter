package com.chatter.chatter.service;

import com.chatter.chatter.dao.LogRepository;
import com.chatter.chatter.dao.UserRepository;
import com.chatter.chatter.dto.Log;
import com.chatter.chatter.dto.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final LogRepository logRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    @Autowired
    public UserService(PasswordEncoder passwordEncoder, UserRepository userRepository, LogRepository logRepository, SessionService sessionService) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.logRepository = logRepository;
        this.sessionService = sessionService;
    }

    public void Register(User user) {
        if (userRepository.findByUsername(user.getUsername()).isEmpty()) {
            String rawPassword = user.getPassword();
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);

            Log log = new Log(user.getUsername(), "Register");
            logRepository.save(log);

            user.setPassword(rawPassword);
        }
        login(user);
    }

    public void login(User user) {
        sessionService.logout();
        if (isValidUser(user.getUsername(), user.getPassword())) {
            System.out.println("Login successful");
            sessionService.setLoggedInUser(user.getUsername());
            Log log = new Log(user.getUsername(), "Login");
            logRepository.save(log);
            System.out.println("Welcome " + user.getUsername());
        } else {
            System.out.println("Login failed");
        }
    }


    public boolean isValidUser(String username, String rawPassword) {
        User foundUser = userRepository.findByUsername(username).orElse(null);
        return foundUser != null && passwordEncoder.matches(rawPassword, foundUser.getPassword());
    }


    public void logout() {
        String username = sessionService.getLoggedInUser();
        if (username != null) {
            sessionService.logout();
            Log log = new Log(username, "Logout");
            logRepository.save(log);
            System.out.println("Goodbye " + username);
        } else {
            System.out.println("No user logged in");
        }
    }

    public boolean checkAuthority(String authority) {
        String username = sessionService.getLoggedInUser();
        if (username != null) {
            User user = userRepository.findByUsername(username).get();
            return user.getAuth().equals(authority);
        }
        return false;
    }

    public Set<User> getUsersFromString(String usersString) {
        Set<User> users = new HashSet<>();
        String[] usernames = usersString.split(",");
        for (String username : usernames) {
            userRepository.findByUsername(username).ifPresent(users::add);
        }
        return users;
    }

    public User getUserByUsername(String username) {
        return userRepository.findByUsername(username).orElse(null);
    }

    public void authorizeAdminAccess() {
        String username = sessionService.getLoggedInUser();
        if (username == null || !checkAuthority("ADMIN")) {
            throw new SecurityException("Access denied: Admins only");
        }
    }
}