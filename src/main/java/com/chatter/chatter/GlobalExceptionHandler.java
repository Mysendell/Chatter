package com.chatter.chatter;

import org.springframework.ui.Model;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handling for SecurityException
     * @param e The exception
     * @param model The model to be sent to the template
     * @return A string containing the template name
     */
    @ExceptionHandler(SecurityException.class)
    public String handleSecurityException(SecurityException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error/403";
    }

    /**
     * Handles IllegalArgumentException
     * @param e The IllegalArgumentException that was thrown
     * @param model The model to be sent to the template
     * @return A string specifying the name of the template to render
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public String handleIllegalArgumentException(IllegalArgumentException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error/bad-request";
    }

    /**
     * Handles MissingServletRequestParameterException.
     *
     * @param e The MissingServletRequestParameterException that was thrown
     * @param model The model to be sent to the template.
     * @return A string specifying the name of the template to render
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public String handleIllegalArgumentException(MissingServletRequestParameterException e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error/bad-request";
    }


    /**
     * In case none of the above handlers catch the exception, this shows a generic error page
     * @param e the Exception
     * @param model The model to be sent to the template
     * @return A string specifying the name of the template to render
     */
    @ExceptionHandler(Exception.class)
    public String handleException(Exception e, Model model) {
        model.addAttribute("error", e.getMessage());
        return "error/error";
    }
}