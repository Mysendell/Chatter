package com.chatter.chatter.service;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.Optional;

/**
 * Service class responsible for managing the session and authentication state of a user.
 * This class interacts with HTTP cookies to handle session information such as logged-in user.
 */
@Service
public class SessionService {

    private final HttpServletRequest request;
    private final HttpServletResponse response;

    @Autowired
    public SessionService(HttpServletRequest request, HttpServletResponse response) {
        this.request = request;
        this.response = response;
    }

    /**
     * Retrieves a cookie by a certain name
     * @param name The name of the cookie
     * @return A cookie wrapped in a optional object
     */
    private Optional<Cookie> getCookie(String name) {
        if (request.getCookies() != null) {
            return Arrays.stream(request.getCookies())
                    .filter(cookie -> cookie.getName().equals(name))
                    .findFirst();
        }
        return Optional.empty();
    }

    /**
     * Returns the name of the user currently logged in
     * @return The name of the logged in user
     */
    public String getLoggedInUser() {
        return getCookie("loggedInUser").map(Cookie::getValue).orElse(null);
    }


    /**
     * Checks if someone is logged in
     * @return A boolean indicating wheter someone is logged in or not
     */
    public boolean isLoggedIn() {
        return getLoggedInUser() != null;
    }


    /**
     * Deletes the cookie that sets a user as logged in, logging them out
     */
    public void logout() {
        Cookie cookie = new Cookie("loggedInUser", null);
        cookie.setMaxAge(0);
        cookie.setPath("/");
        response.addCookie(cookie);
    }


    /**
     * Create a cookie setting someone as logged in
     * @param username The name of the user being logged in
     */
    public void setLoggedInUser(String username) {
        Cookie cookie = new Cookie("loggedInUser", username);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(7 * 24 * 60 * 60);
        response.addCookie(cookie);
    }

    /**
     * Check if a user is logged in, if not throws SecurityException
     * @throws SecurityException if no one is logged in
     */
    public void requireLoggedInUser() {
        if (!isLoggedIn()) {
            throw new SecurityException("User must be logged in");
        }
    }
}