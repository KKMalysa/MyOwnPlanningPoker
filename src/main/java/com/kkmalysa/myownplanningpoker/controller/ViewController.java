package com.kkmalysa.myownplanningpoker.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Controller responsible for rendering the main view of the Planning Poker application.
 */
@Controller
public class ViewController {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
