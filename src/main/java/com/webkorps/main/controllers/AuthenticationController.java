//package com.webkorps.main.controllers;
//
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.authentication.AuthenticationManager;
//import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.userdetails.UserDetails;
//import org.springframework.web.bind.annotation.PostMapping;
//import org.springframework.web.bind.annotation.RequestBody;
//import org.springframework.web.bind.annotation.RequestMapping;
//import org.springframework.web.bind.annotation.RestController;
//
//import com.webkorps.main.DTO.JwtResponse;
//import com.webkorps.main.DTO.LoginRequest;
//import com.webkorps.main.DTO.SignupRequest;
//import com.webkorps.main.Services.CaptchaValidator;
//import com.webkorps.main.Services.JwtService;
//import com.webkorps.main.Services.UserService;
//import com.webkorps.main.entity.User;
//
//import lombok.RequiredArgsConstructor;
//
//@RestController
//@RequestMapping("/api/auth")
//@RequiredArgsConstructor
//public class AuthenticationController {
//
//	@Autowired
//    private  AuthenticationManager authManager;
//    @Autowired
//    private  UserService userService;
//    @Autowired
//    private  JwtService jwtService;
//
//    @Autowired
//    private CaptchaValidator captchaValidator;
//    
//    @PostMapping("/signup")
//    public ResponseEntity<?> signup(@RequestBody SignupRequest request) {
//        // Validate reCAPTCHA token
//        if (!captchaValidator.isCaptchaValid(request.getCaptchaToken())) {
//            return ResponseEntity.badRequest().body("CAPTCHA validation failed.");
//        }
//
//        User user = userService.registerUser(request);
//        return ResponseEntity.ok("User registered with username: " + user.getUsername());
//    }
//
//    @PostMapping("/login")
//    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
//        try {
//            Authentication auth = authManager.authenticate(
//                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
//            );
//
//            UserDetails userDetails = (UserDetails) auth.getPrincipal();
//            String token = jwtService.generateToken(userDetails);
//            User user = userService.getUserByEmail(request.getEmail());
//
//            return ResponseEntity.ok(new JwtResponse(token, user.getUsername(), user.getEmail()));
//        } catch (Exception e) {
//            return ResponseEntity.status(401).body("Invalid email or password.");
//        }
//    }
//
//}
