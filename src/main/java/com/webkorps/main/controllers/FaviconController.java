package com.webkorps.main.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class FaviconController {
    @RequestMapping("favicon.ico")
    public void returnFavicon() {
        // Just return an empty response
    }
}
