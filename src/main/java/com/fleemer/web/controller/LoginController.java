package com.fleemer.web.controller;

import java.security.Principal;
import javax.servlet.http.HttpSession;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/login")
public class LoginController {
    private static final String ROOT_VIEW = "login";

    @GetMapping
    public String login(HttpSession session, Principal principal) {
        String lang = LocaleContextHolder.getLocale().getLanguage();
        session.setAttribute("locale", "en".equals(lang) ? "ru" : "en");
        if (principal != null) {
            return "redirect:/";
        }
        return ROOT_VIEW;
    }
}
