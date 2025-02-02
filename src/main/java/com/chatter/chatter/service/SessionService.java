package com.chatter.chatter.service;

import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SessionService {

    private final HttpSession session;

    @Autowired
    public SessionService(HttpSession session) {
        this.session = session;
    }

    public String getLoggedInUser() {
        return (String) session.getAttribute("loggedInUser");
    }

    public boolean isLoggedIn() {
        return getLoggedInUser() != null;
    }

    public void logout() {
        session.invalidate();
    }

    public void setLoggedInUser(String username) {
        session.setAttribute("loggedInUser", username);
    }
}