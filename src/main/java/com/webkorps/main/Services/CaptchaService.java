package com.webkorps.main.Services;

import org.springframework.stereotype.Service;

import java.util.Random;

@Service
public class CaptchaService {

    private static final String CHAR_POOL = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
    private static final int CAPTCHA_LENGTH = 6;

    // Method to generate a random CAPTCHA string
    public String generateCaptcha() {
        Random random = new Random();
        StringBuilder captcha = new StringBuilder(CAPTCHA_LENGTH);

        for (int i = 0; i < CAPTCHA_LENGTH; i++) {
            int index = random.nextInt(CHAR_POOL.length());
            captcha.append(CHAR_POOL.charAt(index));
        }

        return captcha.toString();
    }
}

