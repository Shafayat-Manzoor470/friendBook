package com.webkorps.main.Services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service // Marks this class as a Spring service component
public class CaptchaValidator {

    @Value("${recaptcha.secret}") // Injects the reCAPTCHA secret key from application properties
    private String recaptchaSecret;

    /**
     * Validates the reCAPTCHA token by sending a request to Google's reCAPTCHA API.
     *
     * @param token the reCAPTCHA response token from the client
     * @return true if the captcha is valid, false otherwise
     */
    public boolean isCaptchaValid(String token) {
        // Google reCAPTCHA verification URL
        String url = "https://www.google.com/recaptcha/api/siteverify";

        // Create a RestTemplate for HTTP communication
        RestTemplate restTemplate = new RestTemplate();

        // Prepare request parameters
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", recaptchaSecret); // Add secret key
        requestMap.add("response", token);         // Add user's captcha response token

        // Wrap parameters in an HttpEntity to send in the request body
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestMap);

        // Send POST request to Google's verification endpoint and get the response
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        // Extract body and return whether the verification was successful
        Map body = response.getBody();
        return body != null && Boolean.TRUE.equals(body.get("success"));
    }
}
