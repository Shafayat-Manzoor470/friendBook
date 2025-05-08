package com.webkorps.main.controllers;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.webkorps.main.CustomExceptions.EmailAlreadyExistsException;
import com.webkorps.main.DTO.ErrorResponse;
import com.webkorps.main.DTO.JwtResponse;
import com.webkorps.main.DTO.LoginRequest;
import com.webkorps.main.DTO.LoginResponse;
import com.webkorps.main.DTO.SignupRequest;
import com.webkorps.main.DTO.UpdateInfoRequest;
import com.webkorps.main.DTO.UserProfileDTO;
import com.webkorps.main.Services.CaptchaService;
import com.webkorps.main.Services.JwtService;
import com.webkorps.main.Services.UserService;
import com.webkorps.main.entity.User;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/api/user") 
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
    public ResponseEntity<?> login(@RequestBody LoginRequest request ) {
        try {
            // Authenticate using the authenticationManager (delegates to UserDetailsService)
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
            );

            // If authentication is successful, generate JWT
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();
            String token = jwtService.generateToken(userDetails);

            return ResponseEntity.ok(new JwtResponse(token, userDetails.getUsername(), request.getEmail(),System.currentTimeMillis()));

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
    public ResponseEntity<?> getProfile(Authentication authentication,HttpSession session) {
        try {
            if (authentication == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("No authentication found");
            }

            
            if (session.getAttribute("loginTime") == null) {
                session.setAttribute("loginTime", LocalDateTime.now());
            }
           
            String username = authentication.getName();
            System.out.println("Fetching profile for on line 100 in authcontroller:  " + username);

            UserProfileDTO user = userService.getUserProfile(username); // <- This has the correct data

            if (user == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User Not Found");
            }
            Map<String, Object> profile = new HashMap<>();
            profile.put("username", user.getUsername());
            profile.put("profilePhotoUrl", user.getProfilePhoto());
            profile.put("favSongs", user.getFavSongs());
            profile.put("favBooks", user.getFavBooks());
            profile.put("favPlaces", user.getFavPlaces());
            LocalDateTime loginTime = (LocalDateTime) session.getAttribute("loginTime");
            profile.put("loginTime", loginTime.toString());
              
                
            
            return ResponseEntity.ok(profile); 
        } catch (Exception e) {
            e.printStackTrace(); 	
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
        }
    }

    
    
    
    
    @PostMapping("/update-info")
    public ResponseEntity<?> updateUserInfo(@RequestBody UpdateInfoRequest updateInfoRequest,
                                             @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid authorization header.");
        }

        try {
            String email = jwtService.extractUserName(token); //  email from JWT.
            userService.updateUserInfo(email, updateInfoRequest); // Call service
            return ResponseEntity.ok("User information updated successfully.");

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user information.");
        }
    }
    
    
    
    
    
    @PostMapping("/upload-photo")
    public ResponseEntity<?> uploadProfilePhoto(@RequestParam("profilePhoto") MultipartFile file,
                                                @RequestHeader("Authorization") String authorizationHeader) {
        String token = extractToken(authorizationHeader);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid authorization header.");
        }

        try {
            String username = jwtService.extractUserName(token);
            
            System.out.println("in AUth controller on line 159 : "+username);

            if (file.isEmpty()) {
                return ResponseEntity.badRequest().body("Please select a file to upload.");
            }

            if (!file.getContentType().startsWith("image/")) {
                return ResponseEntity.badRequest().body("Please upload an image file.");
            }

            String newProfilePhotoUrl = userService.uploadProfilePhoto(username, file);
//             userService.updateProfilePhotoUrl(username, newProfilePhotoUrl);   //===========================================
            System.out.println("in AUth controller on line 170 : "+newProfilePhotoUrl);
            return ResponseEntity.ok(new HashMap<String, String>() {{
                put("newProfilePhotoUrl", newProfilePhotoUrl);
            }});

        } catch (JwtException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
        } catch (Exception e) {
            System.err.println("Error uploading profile photo: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload profile photo.");
        }
    }
    
    private String extractToken(String authorizationHeader) {
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            return authorizationHeader.substring(7);
        }
        return null;
    }

    
    

    @GetMapping("/search/{query}")
    public ResponseEntity<?> searchUser(@PathVariable String query, Authentication authentication) {
        if (authentication == null) {
            return ResponseEntity.status(401).body("Unauthorized");
        }
        String currentUser = authentication.getName();

        try {
            //  Call the userService method to search for users by username.
            List<UserProfileDTO> searchedUsers = userService.searchUsersByUsername(query);

            //  Filter out the current user from the search results.
            List<UserProfileDTO> filteredResults = searchedUsers.stream()
                    .filter(user -> !user.getUsername().equals(currentUser))
                    .collect(Collectors.toList());

            return ResponseEntity.ok(filteredResults);

        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Error during user search: " + e.getMessage()); // Include the exception message
        }
    }
    



























}