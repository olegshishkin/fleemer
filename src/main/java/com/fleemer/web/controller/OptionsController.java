package com.fleemer.web.controller;

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

}
