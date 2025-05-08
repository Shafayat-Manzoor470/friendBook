package com.webkorps.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import com.webkorps.main.DTO.UserProfileDTO;
import com.webkorps.main.Services.JwtService;
import com.webkorps.main.Services.UserService;
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
	    public String serveProfilePage(HttpServletRequest request) {
	        String authHeader = request.getHeader("Authorization");
	        if (authHeader != null && authHeader.startsWith("Bearer ")) {
	            String token = authHeader.substring(7);
	            try {
	                String username = jwtService.extractUserName(token);
	                if (username != null && jwtService.validateToken1(token)) {
	                    return "profile"; // Render templates/profile.html
	                }
	            } catch (Exception e) {
	                e.printStackTrace();
	            }
	        }
	        return "redirect:/login"; // or return a 403 error page
	    }
	 
	 @GetMapping("/home")
	 public String showHome() { 
	     return "home"; 
	 }
}
