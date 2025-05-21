package com.webkorps.main.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.webkorps.main.DTO.UserProfileDTO;
import com.webkorps.main.Services.JwtService;
import com.webkorps.main.Services.PostService;
import com.webkorps.main.Services.UserService;
import com.webkorps.main.entity.Post;
import com.webkorps.main.entity.User;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class UserController {
	
	@Autowired
	private UserService userService;
	
	 @Autowired
	    private JwtService jwtService;
	 
	 @Autowired
	 private PostService postService;

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
	
	
	// =====serve the profile page==========================================
	@GetMapping("/profile")
	public String serveProfilePage(HttpServletRequest request, Model model) {
	    String authHeader = request.getHeader("Authorization");

	    if (authHeader != null && authHeader.startsWith("Bearer ")) {
	        String token = authHeader.substring(7);
	        try {
	            String username = jwtService.extractUserName(token);

	            if (username != null && jwtService.validateToken1(token)) {
	                User user = userService.getUserByEmail(username);
	                List<Post> posts = postService.findByUserId(user.getId());

	                model.addAttribute("user", user);
	                model.addAttribute("posts", posts); //  required for profile.html

	                return "profile"; //  pass data to template
	            }
	        } catch (Exception e) {
	            e.printStackTrace();
	        }
	    }
	    return "redirect:/login";
	}

//===============serve homepage====================================	 
	 @GetMapping("/home")
	 public String showHome() { 
	     return "home"; 
	 }
}
