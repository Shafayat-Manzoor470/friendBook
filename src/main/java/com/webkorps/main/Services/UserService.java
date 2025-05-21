package com.webkorps.main.Services;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path; // Fixed: use java.nio.file.Path, not JPA
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import org.springframework.beans.factory.annotation.Autowired;
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
import com.webkorps.main.entity.FriendRequest;
import com.webkorps.main.entity.User;
import com.webkorps.main.entity.UserFollow;
import com.webkorps.main.repository.FriendRequestRepository;
import com.webkorps.main.repository.UserFollowRepository;
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
	    

	    @Autowired
	    private UserFollowRepository userFollowRepository;
    // Register a new user with encoded password and unique username
	    public User registerUser(SignupRequest request) {
	        // Check if email is already registered
	        if (userRepository.existsByEmail(request.getEmail())) {
	            throw new EmailAlreadyExistsException("Email already registered");
	        }

	        // Generate a unique username from full name
	        String username = generateUsername(request.getFullName());

	        // Ensure username uniqueness
	        while (userRepository.existsByUsername(username)) {
	            username = generateUsername(request.getFullName());
	        }

	        // Create and set user fields
	        User user = new User();
	        user.setFullName(request.getFullName());
	        user.setEmail(request.getEmail());
	        user.setPassword(encoder.encode(request.getPassword()));
	        user.setUsername(username);

	        //=================Save and return the new user=========================
	        return userRepository.save(user);
	    }

	    // ==============================Generate a random username from the user's full name=========================
	    public String generateUsername(String fullName) {
	        String namePart = fullName.replaceAll("[^a-zA-Z]", ""); // Remove non-alphabetic characters
	        if (namePart.length() < 5) {
	            namePart = String.format("%-5s", namePart).replace(" ", "x"); // Pad with 'x' if too short
	        } else {
	            namePart = namePart.substring(0, 5); // Take first 5 characters
	        }
	        namePart = namePart.substring(0, 1).toUpperCase() + namePart.substring(1).toLowerCase(); //======== Capitalize first letter
	        int num = new Random().nextInt(900) + 100; // Random 3-digit number
	        return namePart + num;
	    }

	    @Override
	    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
	        // Fetch user by email
	        User user = userRepository.findByEmail(email)
	            .orElseThrow(() -> new UsernameNotFoundException("User not found"));

	        // Provide default USER authority for Spring Security
	        List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority("USER"));

	        // Return Spring Security User object
	        return new org.springframework.security.core.userdetails.User(
	            user.getEmail(),
	            user.getPassword(),
	            authorities
	        );
	    }

	    //=======================Get full user entity by email=================================
	    public User getUserByEmail(String email) {
	        return userRepository.findByEmail(email).orElse(null);
	    }

	    //=================Compare raw and encoded passwords===============================
	    public boolean passwordMatches(String rawPassword, String encodedPassword) {
	        return encoder.matches(rawPassword, encodedPassword);
	    }

	    @Transactional
	    public UserProfileDTO getUserProfile(String email) {
	        // Find user by email
	        User user = userRepository.findByEmail(email)
	                      .orElseThrow(() -> new UsernameNotFoundException("User not found"));

	        // Return user's profile data as DTO
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

	    @Transactional
	    public void updateUserInfo(String email, UpdateInfoRequest updateInfoRequest) {
	        // Find user by email
	        User user = userRepository.findByEmail(email)
	                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

	        // Update user info from request
	        user.setFavSongs(updateInfoRequest.getFavSongs());
	        user.setFavBooks(updateInfoRequest.getFavBooks());
	        user.setFavPlaces(updateInfoRequest.getFavPlaces());

	        // Save updated user
	        userRepository.save(user);
	    }

	    
	    //======================upload profile photo service ============================================
	    public String uploadProfilePhoto(String username, MultipartFile file) throws StorageException, ImageProcessingException {
	        try {
	            if (file.isEmpty()) {
	                throw new ImageProcessingException("Uploaded file is empty.");
	            }

	            //====================== Validate file extension=============================
	            String fileExtension = StringUtils.getFilenameExtension(file.getOriginalFilename());
	            if (fileExtension == null || !List.of("jpg", "jpeg", "png").contains(fileExtension.toLowerCase())) {
	                throw new ImageProcessingException("Invalid file type. Only JPG, JPEG, and PNG are supported.");
	            }

	            //====================== Create unique file name===========================================
	            String uniqueFileName = UUID.randomUUID().toString() + "." + fileExtension;
	            Path uploadPath = Paths.get(uploadDir);

	            //=================== Create upload directory if not exists=================================
	            if (!Files.exists(uploadPath)) {
	                Files.createDirectories(uploadPath);
	            }

	            //=========================Save file to disk================================================
	            try (InputStream inputStream = file.getInputStream()) {
	                Path filePath = uploadPath.resolve(uniqueFileName);
	                Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
	                String profilePhotoUrl = "/uploads/" + uniqueFileName;

	                //===================Update user's profile photo URL=====================================
	                User user = userRepository.findByEmail(username)
	                        .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
	                System.out.println("in User Service on line 167: " + username);
	                System.out.println("in User Service on line 168: " + user.toString());
	                user.setProfilePhoto(profilePhotoUrl);
	                userRepository.save(user);

	                return profilePhotoUrl;
	            } catch (IOException e) {
	                throw new StorageException("Could not save the file: " + uniqueFileName, e);
	            }
	        } catch (IOException e) {
	            throw new ImageProcessingException("Error reading uploaded file.", e);
	        } catch (ImageProcessingException e) {
	            //=====================Log image processing error============================================
	            e.printStackTrace();
	        }

	        return null;
	    }

	    //==============================Update only profile photo URL for user======================================
	    public void updateProfilePhotoUrl(String username, String photoUrl) {
	        User user = userRepository.findByUsername(username)
	            .orElseThrow(() -> new UsernameNotFoundException("User not found"));
	        user.setProfilePhoto(photoUrl);
	        userRepository.save(user);
	    }

	    @Transactional // Use transaction for read consistency
	    public List<UserProfileDTO> searchUsersByUsername(String query) {
	        //================= Search users with usernames containing the query string==================================
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
	                        user.getFollowers().size(),
	                        user.getFollowing().size()
	                ))
	                .collect(Collectors.toList());
	    }

	
	 @Autowired
	    private FriendRequestRepository friendRequestRepository;
	 @Transactional
	    public void sendFriendRequest(long toUserId, long fromUserId) {
	        User fromUser = userRepository.findById(fromUserId)
	            .orElseThrow(() -> new RuntimeException("Sender not found"));
	        User toUser = userRepository.findById(toUserId)
	            .orElseThrow(() -> new RuntimeException("Receiver not found"));

	        if (friendRequestRepository.findByFromUserAndToUser(fromUser, toUser) != null) {
	            throw new RuntimeException("Friend request already sent");
	        }

	        FriendRequest request = new FriendRequest();
	        request.setFromUser(fromUser);
	        request.setToUser(toUser);
	        request.setStatus("PENDING");
	        friendRequestRepository.save(request);
	    }

	//==============================Get UserProfileDTO by ID==================================================
	@Transactional
	public UserProfileDTO getUserProfileById(long userId) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
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

	//====================================== Get a list of UserProfileDTOs for the followers of a given user ID================================
	@Transactional
	public List<UserProfileDTO> getUserFollowers(long userId) {
	    User user = userRepository.findById(userId)
	            .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
	    return user.getFollowers().stream()
	            .map(follower -> new UserProfileDTO(
	                    follower.getId(),
	                    follower.getFullName(),
	                    follower.getUsername(),
	                    follower.getEmail(),
	                    follower.getProfilePhoto(),
	                    follower.getFavBooks(),
	                    follower.getFavPlaces(),
	                    follower.getFavSongs(),
	                    follower.getFollowers().size(),
	                    follower.getFollowing().size()
	            ))
	            .collect(Collectors.toList());
	}

	//============================== Check if userA (loggedInUserId) is following userB (followerId)===========================================
	@Transactional
	public boolean isFollowing(long userAId, long userBId) {
	    User userA = userRepository.findById(userAId)
	            .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userAId));
	    User userB = userRepository.findById(userBId)
	            .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userBId));
	    return userA.getFollowing().contains(userB);
	}
	
	
	
	// ===============================Accept Friend Request - Add follower/following relationship==============================
    public void acceptFriendRequest(String currentUsername, Long targetUserId) {
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));
        User targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new RuntimeException("Target user not found"));

        //================================Add to followers and following=====================================
        currentUser.getFollowing().add(targetUser);
        targetUser.getFollowers().add(currentUser);

        //==================Save both users with the updated relationships======================================
        userRepository.save(currentUser);
        userRepository.save(targetUser);
    }

    //=============================Reject Friend Request - Remove from followers and following===========================
    public void rejectFriendRequest(String currentUsername, Long targetUserId) {
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));
        User targetUser = userRepository.findById(targetUserId).orElseThrow(() -> new RuntimeException("Target user not found"));

        //====================Remove from followers and following========================================
        currentUser.getFollowing().remove(targetUser);
        targetUser.getFollowers().remove(currentUser);

        //==========================Save both users with the updated relationships===================================
        userRepository.save(currentUser);
        userRepository.save(targetUser);
    }

    //=============================Get Notifications (could be replaced with actual Notification service)=========================
    public List<User> getFriendRequests(String currentUsername) {
        User currentUser = userRepository.findByUsername(currentUsername).orElseThrow(() -> new RuntimeException("User not found"));
        return (List<User>) currentUser.getFollowing(); // Placeholder for pending friend requests
    }

    
    @Transactional
    public void respondToFriendRequest(String currentUsername, Long fromUserId, boolean accept) {
        User toUser = userRepository.findByEmail(currentUsername)
            .orElseThrow(() -> new RuntimeException("User not found"));
        System.out.println("To User: " + toUser);

        User fromUser = userRepository.findById(fromUserId)
            .orElseThrow(() -> new RuntimeException("Sender not found"));
        System.out.println("From User: " + fromUser);

        //====================== Fetch the friend request======================================
        FriendRequest request = friendRequestRepository.findByFromUserAndToUser(fromUser, toUser);
        if (request == null) {
            throw new RuntimeException("Friend request not found");
        }

        if (accept) {
            //=====================Update the friend request status to accepted===================================
            request.setStatus("ACCEPTED");
            friendRequestRepository.save(request);

            //=====================Add follower/following relationship=============================================
            toUser.getFollowing().add(fromUser);
            fromUser.getFollowers().add(toUser);

            userRepository.save(toUser);
            userRepository.save(fromUser);
        } else {
            //===============Update the friend request status to rejected===========================================
            request.setStatus("REJECTED");
            friendRequestRepository.save(request);
        }
    }

	public Optional<User> getUserById(long loggedInUserId) {
		return userRepository.findById(loggedInUserId);
	}
	@Transactional
	public void followUser(User follower, User followed) {
	    UserFollow userFollow = new UserFollow();
	    
	    userFollow.setFollower(follower);  
	    userFollow.setFollowed(followed);  

	    userFollowRepository.save(userFollow);

	    // update bidirectional relationships
	    follower.getFollowing().add(followed);
	    followed.getFollowers().add(follower);

	    userRepository.save(follower);
	    userRepository.save(followed);
	}
	@Transactional
	public void followUser(long followerId, long followedId) {
	    User follower = userRepository.findById(followerId)
	        .orElseThrow(() -> new UsernameNotFoundException("Follower not found"));

	    User followed = userRepository.findById(followedId)
	        .orElseThrow(() -> new UsernameNotFoundException("User to follow not found"));

	    //=========================Add the following relationship===============================
	    follower.getFollowing().add(followed);

	    //============================Save follower=============================================
	    userRepository.save(follower);

	    // ================== save user_following entry too=====================================
	    UserFollow userFollow = new UserFollow();
	    userFollow.setFollower(follower);
	    userFollow.setFollowed(followed);
	    userFollowRepository.save(userFollow);
	}
	
	//===============get user who logged in=======================================
	public User getCurrentUser() {
	    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
	    if (authentication == null || !authentication.isAuthenticated()) {
	        return null;
	    }
	    String email = authentication.getName();
	    return userRepository.findByEmail(email).orElse(null);
	}

	public Optional<User> findByUsername(String username) {
		return userRepository.findByEmail(username);
	}
	}


	
	




