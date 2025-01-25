package com.chatter.chatter;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebController {

    @GetMapping("/login")
    public String login(){
        return "login";
    }
    @PostMapping("/login")
    public String handleLogin(@ModelAttribute User user){
        UserService.saveUser(user);
        return "redirect:/home";
    }

    @GetMapping("")
    public String indedx(){
        return "index";
    }
}
