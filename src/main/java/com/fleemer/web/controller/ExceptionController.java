package com.fleemer.web.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class ExceptionController {
    private static final String ROOT_VIEW = "error";

    @ExceptionHandler(Exception.class)
    public String show() {
        return ROOT_VIEW;
    }
}
