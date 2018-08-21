package com.fleemer.web.controller;

import java.security.Principal;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/login")
public class LoginController {
    private static final String ROOT_VIEW = "login";

    @GetMapping
    public String login(@RequestParam(value = "lang", required = false) String lang, Model model, Principal principal) {
        if (lang == null || lang.isEmpty()) {
            String curLang = LocaleContextHolder.getLocale().getLanguage();
            model.addAttribute("locale", curLang.equals("en") ? "ru" : "en");
        } else {
            model.addAttribute("locale", lang);
        }
        if (principal != null) {
            return "redirect:/";
        }
        return ROOT_VIEW;
    }
}
