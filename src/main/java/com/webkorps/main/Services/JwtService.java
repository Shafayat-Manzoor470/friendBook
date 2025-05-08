package com.webkorps.main.Services;

import java.security.Key;
import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.stereotype.Service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;


@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private long jwtExpiration;

    private Key key;
    
    @Autowired
    @Lazy
    private   UserDetailsService userDetailsService;

    @PostConstruct
    public void init() {
        // Convert the secret key (string) into a secure HMAC key
        this.key = Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }

    // Generate JWT token for the given user
    public String generateToken(UserDetails userDetails) {
        return Jwts.builder()
                .setSubject(userDetails.getUsername())
                .setIssuer("Friend-Book")
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();
    }

    // Extract username (subject) from JWT token
    public String extractUserName(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            System.err.println("JWT extraction error: " + e.getMessage());
            return null;
        }
    }

    // Check if token is valid and not expired
    public boolean validateToken(String token, UserDetails userDetails) {
        String username = extractUserName(token);
        return username != null &&
               username.equals(userDetails.getUsername()) &&
               !isTokenExpired(token);
    }

    // Check if token is expired
    public boolean isTokenExpired(String token) {
        try {
            Date expiration = Jwts.parserBuilder()
                    .setSigningKey(key)
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getExpiration();
            return expiration.before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return true;
        }
    }
    
    public boolean validateToken1(String token) {
        String username = extractUserName(token);
      
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return validateToken(token, userDetails);
    }
}
