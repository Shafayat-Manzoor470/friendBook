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

@Service
public class CaptchaValidator {

    @Value("${recaptcha.secret}")
    private String recaptchaSecret;

    public boolean isCaptchaValid(String token) {
        String url = "https://www.google.com/recaptcha/api/siteverify";

        RestTemplate restTemplate = new RestTemplate();
        MultiValueMap<String, String> requestMap = new LinkedMultiValueMap<>();
        requestMap.add("secret", recaptchaSecret);
        requestMap.add("response", token);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(requestMap);
        ResponseEntity<Map> response = restTemplate.postForEntity(url, request, Map.class);

        Map body = response.getBody();
        return body != null && Boolean.TRUE.equals(body.get("success"));
    }
}
