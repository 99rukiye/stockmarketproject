package com.stockmarketproject.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.security.Principal;

@Controller
public class PageController {

    @GetMapping({"/", "/app"})
    public String app(Model model, Principal principal) {
        model.addAttribute("userEmail", principal != null ? principal.getName() : "-");
        return "app";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }
}
