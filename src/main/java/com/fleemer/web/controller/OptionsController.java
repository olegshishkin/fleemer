package com.fleemer.web.controller;

import javax.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/options")
public class OptionsController {
    private static final String SERIALIZE_VIEW = "serialize";

    @GetMapping("/serialize")
    public String serialize() {
        return SERIALIZE_VIEW;
    }

    @GetMapping("/locale")
    public String localize(@RequestParam(value = "lang", required = false) String lang, HttpSession session) {
        session.setAttribute("switchLocale", "en".equals(lang) ? "ru" : "en");
        return "redirect:/login" + ((lang == null || lang.isEmpty()) ? "" : "?lang=" + lang);
    }
}
