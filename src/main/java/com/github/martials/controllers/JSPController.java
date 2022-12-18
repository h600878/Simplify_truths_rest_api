package com.github.martials.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class JSPController {

    @GetMapping("/")
    public String getIndexPage() {
        return "index";
    }

}
