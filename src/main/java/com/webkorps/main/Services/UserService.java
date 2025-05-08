package com.webkorps.main.Services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path; // Fixed: use java.nio.file.Path, not JPA
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.webkorps.main.CustomExceptions.EmailAlreadyExistsException;
import com.webkorps.main.CustomExceptions.ImageProcessingException;
import com.webkorps.main.CustomExceptions.StorageException;
import com.webkorps.main.DTO.SignupRequest;
import com.webkorps.main.DTO.UpdateInfoRequest;
import com.webkorps.main.DTO.UserProfileDTO;
import com.webkorps.main.entity.User;
import com.webkorps.main.repository.UserRepository;

@Service
public class UserService implements UserDetailsService {
	
	 private final UserRepository userRepository;
	    private final PasswordEncoder encoder;

	    private static final String uploadDir = "uploads";

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
    
    
    @Transactional
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

    
    //======================================================================
    @Transactional
    public void updateUserInfo(String email, UpdateInfoRequest updateInfoRequest) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Update the user's fields with the new values from the request.
        user.setFavSongs(updateInfoRequest.getFavSongs());
        user.setFavBooks(updateInfoRequest.getFavBooks());
        user.setFavPlaces(updateInfoRequest.getFavPlaces());

        // Save the updated User object back to the database.
        userRepository.save(user);
    }

//================================================================================
    public String uploadProfilePhoto(String username, MultipartFile file) throws StorageException, ImageProcessingException {
        try {
            if (file.isEmpty()) {
                throw new ImageProcessingException("Uploaded file is empty.");
            }

            String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
            if (fileExtension == null || !List.of("jpg", "jpeg", "png").contains(fileExtension.toLowerCase())) {
                throw new ImageProcessingException("Invalid file type. Only JPG, JPEG, and PNG are supported.");
            }

            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
            Path uploadPath = Paths.get(uploadDir);

            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            try (InputStream inputStream = file.getInputStream()) {
                Path filePath = uploadPath.resolve(uniqueFileName);
                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
                String profilePhotoUrl = "/uploads/" + uniqueFileName;

                User user = userRepository.findByEmail(username)
                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
                System.out.println("in User Service on line 167: "+username);
                System.out.println("in User Service on line 168: "+user.toString());
                user.setProfilePhoto(profilePhotoUrl);
                userRepository.save(user);

                return profilePhotoUrl;
            } catch (IOException e) {
                throw new StorageException("Could not save the file: " + uniqueFileName, e);
            }
        } catch (IOException e) {
            throw new ImageProcessingException("Error reading uploaded file.", e);
        } catch (ImageProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        return null;
    }
    
    public void updateProfilePhotoUrl(String username, String photoUrl) {
        User user = userRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        user.setProfilePhoto(photoUrl);
        userRepository.save(user);
    }

    @Transactional(readOnly = true) // Add read-only transaction
    public List<UserProfileDTO> searchUsersByUsername(String query) {
        //  Use Spring Data JPA's findByUsernameContainingIgnoreCase (or your preferred method)
        return userRepository.findByUsernameContainingIgnoreCase(query).stream()
                .map(user -> new UserProfileDTO(
                        user.getId(),
                        user.getFullName(),
                        user.getUsername(),
                        user.getEmail(),
                        user.getProfilePhoto(),
                        user.getFavBooks(),
                        user.getFavPlaces(),
                        user.getFavSongs(),
                        user.getFollowers().size(), // Get follower count from the entity
                        user.getFollowing().size()  // Get following count from the entity
                ))
                .collect(Collectors.toList());
    }

}



