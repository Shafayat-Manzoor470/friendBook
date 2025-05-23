package com.webkorps.main.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
public class JwtResponse {
    private String token;
    private String username;
    private String email;
    private long loginTime;
    public JwtResponse(String token) {
        this.token = token;
    }
    
	public JwtResponse(String token, String username, String email,long loginTime) {
		this.token = token;
		this.username = username;
		this.email = email;
		this. loginTime= loginTime;
	}
	
	
	public long getLoginTime() {
		return loginTime;
	}

	public void setLoginTime(long loginTime) {
		this.loginTime = loginTime;
	}

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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
    
    
    
}
