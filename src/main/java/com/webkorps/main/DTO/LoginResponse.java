package com.webkorps.main.DTO;

public class LoginResponse {
    private String token;
    private String username;
    private long loginTime; // Epoch millis

    // Getters and setters
    public String getToken() {
        return token;
    }
    public void setToken(String token) {
        this.token = token;
    }

    public String getUsername() {
        return username;
    }
    public void setUsername(String username) {
        this.username = username;
    }

    public long getLoginTime() {
        return loginTime;
    }
    public void setLoginTime(long loginTime) {
        this.loginTime = loginTime;
    }
}