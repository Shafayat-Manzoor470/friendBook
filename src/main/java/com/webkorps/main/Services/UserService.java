package com.webkorps.main.Services;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.webkorps.main.CustomExceptions.EmailAlreadyExistsException;
import com.webkorps.main.DTO.SignupRequest;
import com.webkorps.main.DTO.UserProfileDTO;
import com.webkorps.main.entity.User;
import com.webkorps.main.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final PasswordEncoder encoder;

    public UserService(UserRepository userRepository, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.encoder = encoder;
    }

    // Register a new user with encoded password and unique username
    public User registerUser(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException("Email already registered");
        }

        String username = generateUsername(request.getFullName());

        while (userRepository.existsByUsername(username)) {
            username = generateUsername(request.getFullName());
        }

        User user = new User();
        user.setFullName(request.getFullName());
        user.setEmail(request.getEmail());
        user.setPassword(encoder.encode(request.getPassword()));
        user.setUsername(username);

        return userRepository.save(user);
    }

    // Generate unique username
    public String generateUsername(String fullName) {
        String namePart = fullName.replaceAll("[^a-zA-Z]", "");
        if (namePart.length() < 5) {
            namePart = String.format("%-5s", namePart).replace(" ", "x");
        } else {
            namePart = namePart.substring(0, 5);
        }
        namePart = namePart.substring(0, 1).toUpperCase() + namePart.substring(1).toLowerCase();
        int num = new Random().nextInt(900) + 100;
        return namePart + num;
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // Give a dummy authority just to make Spring Security happy
        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER"));

        return new org.springframework.security.core.userdetails.User(
            user.getEmail(),
            user.getPassword(),
            authorities
        );
    }

    // Retrieve full user entity by email
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
    
    

    // Check raw password vs. encoded
    public boolean passwordMatches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }
    
    
    public UserProfileDTO getUserProfile(String email) {
        User user = userRepository.findByEmail(email)
                      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        return new UserProfileDTO(
                user.getId(),
                user.getFullName(),
                user.getUsername(),
                user.getEmail(),
                user.getProfilePhoto(),
                user.getFavBooks(),
                user.getFavPlaces(),
                user.getFavSongs(),
                user.getFollowers().size(),
                user.getFollowing().size()
        );
    }

}
