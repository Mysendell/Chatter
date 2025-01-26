package com.chatter.chatter.service;

import com.chatter.chatter.dao.LogRepository;
import com.chatter.chatter.dao.UserRepository;
import com.chatter.chatter.dto.Log;
import com.chatter.chatter.dto.User;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private LogRepository logRepository;
    @Autowired
    private HttpSession session;

    public void saveUser(User user) {
        if (userRepository.findByUsername(user.getUsername()).isEmpty()) {
            userRepository.save(user);
            Log log = new Log(user.getUsername(), "Register");
            logRepository.save(log);
        }
        login(user);
    }

    public void login(User user) {
        if (userRepository.findByUsernameAndPassword(user.getUsername(), user.getPassword()).isPresent()){
            System.out.println("Login successful");
            session.setAttribute("loggedInUser", user.getUsername());
            Log log = new Log(user.getUsername(), "Login");
            logRepository.save(log);
            System.out.println("Welcome " + user.getUsername());
        } else {
            System.out.println("Login failed");
        }
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
            return user.getAuthority().equals(authority);
        }
        return false;
    }
}