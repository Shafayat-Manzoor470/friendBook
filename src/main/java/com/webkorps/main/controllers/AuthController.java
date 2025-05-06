package com.webkorps.main.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.webkorps.main.CustomExceptions.EmailAlreadyExistsException;
import com.webkorps.main.DTO.ErrorResponse;
import com.webkorps.main.DTO.JwtResponse;
import com.webkorps.main.DTO.LoginRequest;
import com.webkorps.main.DTO.SignupRequest;
import com.webkorps.main.DTO.UserProfileDTO;
import com.webkorps.main.Services.CaptchaService;
import com.webkorps.main.Services.JwtService;
import com.webkorps.main.Services.UserService;
import com.webkorps.main.entity.User;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user") // Keeping /api/user as the base for these endpoints
public class AuthController {

    @Autowired
    private UserService userService;

    @Autowired
    private CaptchaService captchaService;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtService jwtService;

    // POST: User signup with CAPTCHA validation
    @PostMapping("/signup")
    public ResponseEntity<?> signup(@RequestBody SignupRequest request, HttpSession session) {
        String sessionCaptcha = (String) session.getAttribute("captcha");

        if (sessionCaptcha == null || !sessionCaptcha.equalsIgnoreCase(request.getCaptchaToken())) {
            return ResponseEntity.badRequest().body("CAPTCHA validation failed.");
        }

        try {
            User user = userService.registerUser(request);
            return ResponseEntity.ok("User registered successfully with username: " + user.getUsername());
        } catch (EmailAlreadyExistsException e) {
            return ResponseEntity.badRequest().body("Email already exists.");
        }
    }

    // Generate CAPTCHA
    @GetMapping("/generate-captcha")
    public ResponseEntity<String> generateCaptcha(HttpSession session) {
        String captcha = captchaService.generateCaptcha();
        session.setAttribute("captcha", captcha);
        return ResponseEntity.ok(captcha);
    }
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // Authenticate using the authenticationManager (delegates to UserDetailsService)
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // If authentication is successful, generate JWT
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            // Return the JWT token and user details
            return ResponseEntity.ok(new JwtResponse(token, userDetails.getUsername(), request.getEmail()));

        } catch (BadCredentialsException e) {
            // If credentials are invalid, return unauthorized status
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new ErrorResponse("Invalid credentials."));
        } catch (Exception e) {
            // Handle any other exceptions, return internal server error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Login failed. Please try again later."));
        }
    }

    
    	
    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication authentication) {
        try {
            String username = authentication.getName(); // Extracted from Spring context

            UserProfileDTO profile = userService.getUserProfile(username);
            if (profile == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
            }

            return ResponseEntity.ok(profile);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }


}