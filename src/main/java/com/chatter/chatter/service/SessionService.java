package com.chatter.chatter.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

@Service
public class SessionService {

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @Autowired
    public SessionService(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    private Optional<Cookie> getCookie(String name) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(name))
                    .findFirst();
        }
        return Optional.empty();
    }

    public String getLoggedInUser() {
        return getCookie("loggedInUser").map(Cookie::getValue).orElse(null);
    }


    public boolean isLoggedIn() {
        return getLoggedInUser() != null;
    }


    public void logout() {
        Cookie cookie = new Cookie("loggedInUser", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }


    public void setLoggedInUser(String username) {
        Cookie cookie = new Cookie("loggedInUser", username);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    public void requireLoggedInUser() {
        if (!isLoggedIn()) {
            throw new SecurityException("User must be logged in");
        }
    }
}