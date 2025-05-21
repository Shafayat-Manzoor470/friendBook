//package com.webkorps.main.controllers;
//
//import com.webkorps.main.Services.CaptchaService;
//
//import jakarta.servlet.http.HttpSession;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.GetMapping;
//
//                         not in use
//@Controller
//public class CaptchaController {
//
//    @Autowired
//    private CaptchaService captchaService;
//
//    @GetMapping("/captcha")
//    public String generateCaptcha(HttpSession session) {
//        String captcha = captchaService.generateCaptcha();
//        session.setAttribute("captcha", captcha); // Store the generated CAPTCHA in the session
//        return "signup"; // Return the signup page
//    }
//}