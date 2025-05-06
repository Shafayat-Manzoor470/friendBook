package com.webkorps.main.controllers;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.webkorps.main.DTO.UserProfileDTO;
import com.webkorps.main.Services.JwtService;
import com.webkorps.main.Services.UserService;
import com.webkorps.main.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserController {
	
	@Autowired
	private UserService userService;
	
	 @Autowired
	    private JwtService jwtService;

	// load the signup page
	@GetMapping("/signup")
	public String showSignupPage() {
		return "signup"; // This will map to /templates/signup.html
	}

	// load the login page
	@GetMapping("/login")
	public String showLogin() {

		return "login";
	}	
	
	 @GetMapping("/profile")
	    public ResponseEntity<UserProfileDTO> getUserProfile(HttpServletRequest request) {
	        String authHeader = request.getHeader("Authorization");
	        if (authHeader != null && authHeader.startsWith("Bearer ")) {
	            String token = authHeader.substring(7);
	            String username = jwtService.extractUserName(token);
	            if (username != null) {
	                UserProfileDTO profile = userService.getUserProfile(username);
	                return ResponseEntity.ok(profile);
	            }
	        }
	        return ResponseEntity.status(401).build(); // Unauthorized
	    }
}
