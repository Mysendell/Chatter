package com.chatter.chatter;

import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error/403";
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error/bad-request";
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleIllegalArgumentException(MissingServletRequestParameterException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error/bad-request";
    }


    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error/error";
    }
}