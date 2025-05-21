package com.webkorps.main.Services;

import java.security.Key;
import java.util.Date;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.webkorps.main.entity.User;
import com.webkorps.main.repository.UserFollowRepository;
import com.webkorps.main.repository.UserRepository;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Service // Declares this class as a Spring Service component
public class JwtService {

    @Value("${jwt.secret}") // Injects JWT secret key from application properties
    private String jwtSecret;

    @Value("${jwt.expiration}") // Injects JWT expiration time from application properties
    private long jwtExpiration;
    
    @Autowired
    private UserRepository repository;

    private Key key; // HMAC signing key used for signing JWT tokens
    
    @Autowired
    @Lazy // Prevents circular dependency issues
    private UserDetailsService userDetailsService;
    
    @Autowired
    private UserFollowRepository followRepository;

    @PostConstruct
    public void init() {
        // Initializes the signing key using the configured secret string
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Generates a JWT token using Spring Security's UserDetails
    public String generateToken(UserDetails userDetails) {
        String email = userDetails.getUsername(); // Email is used as the subject
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return Jwts.builder()
                .setSubject(user.getEmail()) // Set email as subject
                .claim("userId", user.getId()) // Add user ID as custom claim
                .setIssuer("Friend-Book") // Set issuer name
                .setIssuedAt(new Date()) // Set issue time
                .claim("loginTime", new Date()) //  add login time
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration)) // Set expiration
                .signWith(key, SignatureAlgorithm.HS512) // Sign the token with key and algorithm
                .compact(); // Generate compact JWT string
    }
    
    // Generates a JWT token using User entity directly
    public String generateToken(User user) {
        return Jwts.builder()
                .setSubject(user.getUsername())
                .claim("userId", user.getId())
                .setIssuer("Friend-Book")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // Extracts the username (subject) from a JWT token
    public String extractUserName(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject(); // Returns the subject (username/email)
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT extraction error: " + e.getMessage());
            return null;
        }
    }

    // Validates if the token belongs to the provided user and is not expired
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUserName(token);
        return username != null &&
               username.equals(userDetails.getUsername()) &&
               !isTokenExpired(token); // Check expiry
    }

    // Checks if the token is expired
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date()); // Compares with current time
        } catch (JwtException | IllegalArgumentException e) {
            return true; // Treat parsing errors as expiration
        }
    }
    
    // Validates token and loads UserDetails internally
    public boolean validateToken1(String token) {
        String username = extractUserName(token);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return validateToken(token, userDetails);
    }

    // Extracts userId from the JWT token's claims
    public String getUserIdFromToken(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
            Object userId = claims.get("userId"); // Extract custom userId claim
            return userId != null ? userId.toString() : null;
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT claim extraction error: " + e.getMessage());
            return null;
        }
    }

    // Extracts full user profile from token including follower/following count
    public Map<String, Object> getFullUserProfile(String token) {
        try {
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            Object userIdObj = claims.get("userId");
            if (userIdObj == null) {
                throw new IllegalArgumentException("Token does not contain userId claim.");
            }

            Long userId = Long.parseLong(userIdObj.toString());

            User user = repository.findById(userId)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found"));

            int followerCount = followRepository.countByFollowed(user); // Count users following this user
            int followingCount = followRepository.countByFollower(user); // Count users this user follows

            Map<String, Object> profile = Map.of(
                    "id", user.getId(),
                    "fullName", user.getFullName(),
                    "username", user.getUsername(),
                    "email", user.getEmail(),
                    "profilePhoto", user.getProfilePhoto(),
                    "favBooks", user.getFavBooks(),
                    "favPlaces", user.getFavPlaces(),
                    "favSongs", user.getFavSongs(),
                    "followerCount", followerCount,
                    "followingCount", followingCount
            );

            return profile;

        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("Error extracting full user profile: " + e.getMessage());
            return Map.of("error", "Invalid or expired token");
        }
    }
}
