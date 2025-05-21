package com.webkorps.main.controllers;

import java.io.IOException;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Repository;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.webkorps.main.CustomExceptions.EmailAlreadyExistsException;
import com.webkorps.main.DTO.CommentDTO;
import com.webkorps.main.DTO.ErrorResponse;
import com.webkorps.main.DTO.JwtResponse;
import com.webkorps.main.DTO.LoginRequest;
import com.webkorps.main.DTO.PostDTO;
import com.webkorps.main.DTO.SignupRequest;
import com.webkorps.main.DTO.UpdateInfoRequest;
import com.webkorps.main.DTO.UserProfileDTO;
import com.webkorps.main.Services.CaptchaService;
import com.webkorps.main.Services.CommentService;
import com.webkorps.main.Services.JwtService;
import com.webkorps.main.Services.LikeService;
import com.webkorps.main.Services.PostService;
import com.webkorps.main.Services.UserService;
import com.webkorps.main.entity.Comment;
import com.webkorps.main.entity.FriendRequest;
import com.webkorps.main.entity.Post;
import com.webkorps.main.entity.User;
import com.webkorps.main.entity.UserFollow;
import com.webkorps.main.repository.FriendRequestRepository;
import com.webkorps.main.repository.UserFollowRepository;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.http.HttpServletRequest;
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

	@Autowired
	private FriendRequestRepository friendRequestRepository;

	@Autowired
	UserFollowRepository userFollowRepository;

	// ==================================SignUp controller=====================================================================
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

	// ==================== Generate CAPTCHA controller===============================================
	@GetMapping("/generate-captcha")
	public ResponseEntity<String> generateCaptcha(HttpSession session) {
		String captcha = captchaService.generateCaptcha();
		session.setAttribute("captcha", captcha);
		return ResponseEntity.ok(captcha);
	}

//===============================validate login=========================================================
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
		try {
			// Authenticate using the authenticationManager (delegates to
			// UserDetailsService)
			Authentication authentication = authenticationManager
					.authenticate(new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));

			// If authentication is successful, generate JWT
			UserDetails userDetails = (UserDetails) authentication.getPrincipal();
			String token = jwtService.generateToken(userDetails);

			// Set loginTime in session
			session.setAttribute("loginTime", LocalDateTime.now());
			return ResponseEntity.ok(
					new JwtResponse(token, userDetails.getUsername(), request.getEmail(), System.currentTimeMillis()));

		} catch (BadCredentialsException e) {
			// If credentials are invalid, return unauthorized status
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new ErrorResponse("Invalid credentials."));
		} catch (Exception e) {
			// Handle any other exceptions, return internal server error
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
					.body(new ErrorResponse("Login failed. Please try again later."));
		}
	}

//==================update info in profile page====================================================
	@PostMapping("/update-info")
	public ResponseEntity<?> updateUserInfo(@RequestBody UpdateInfoRequest updateInfoRequest,
			@RequestHeader("Authorization") String authorizationHeader) {
		String token = extractToken(authorizationHeader);
		if (token == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid authorization header.");
		}

		try {
			String email = jwtService.extractUserName(token); // email from JWT.
			userService.updateUserInfo(email, updateInfoRequest); // Call service
			return ResponseEntity.ok("User information updated successfully.");

		} catch (JwtException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to update user information.");
		}
	}

//====================upload profile photo==========================
	@PostMapping("/upload-photo")
	public ResponseEntity<?> uploadProfilePhoto(@RequestParam("profilePhoto") MultipartFile file,
			@RequestHeader("Authorization") String authorizationHeader) {
		String token = extractToken(authorizationHeader);
		if (token == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Missing or invalid authorization header.");
		}

		try {
			String username = jwtService.extractUserName(token);

			

			if (file.isEmpty()) {
				return ResponseEntity.badRequest().body("Please select a file to upload.");
			}

			if (!file.getContentType().startsWith("image/")) {
				return ResponseEntity.badRequest().body("Please upload an image file.");
			}

			String newProfilePhotoUrl = userService.uploadProfilePhoto(username, file);
			return ResponseEntity.ok(new HashMap<String, String>() {
				{
					put("newProfilePhotoUrl", newProfilePhotoUrl);
				}
			});

		} catch (JwtException e) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid or expired token.");
		} catch (Exception e) {
			System.err.println("Error uploading profile photo: " + e.getMessage());
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload profile photo.");
		}
	}

//================extract without bearer====================================
	private String extractToken(String authorizationHeader) {
		if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
			return authorizationHeader.substring(7);
		}
		return null;
	}

//==========================search method================================================================
	@GetMapping("/search/{query}")
	public ResponseEntity<?> searchUser(@PathVariable String query, Authentication authentication) {
		if (authentication == null) {
			return ResponseEntity.status(401).body("Unauthorized");
		}
		String currentUser = authentication.getName();

		try {
			// Call the userService method to search for users by username.
			List<UserProfileDTO> searchedUsers = userService.searchUsersByUsername(query);

			// Filter out the current user from the search results.
			List<UserProfileDTO> filteredResults = searchedUsers.stream()
					.filter(user -> !user.getUsername().equals(currentUser)).collect(Collectors.toList());

			return ResponseEntity.ok(filteredResults);

		} catch (Exception e) {
			return ResponseEntity.internalServerError().body("Error during user search: " + e.getMessage());

		}
	}

//==========================Send friend Request==========================================================
	@PostMapping("/send-request/{userId}")
	public ResponseEntity<?> sendRequest(@PathVariable long userId, HttpServletRequest request) {

		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return new ResponseEntity<>("Invalid User", HttpStatus.UNAUTHORIZED);
		}

		String token = authHeader.substring(7);
		String senderId = jwtService.getUserIdFromToken(token);

		try {
			long senderUserId = Long.parseLong(senderId);
			if (senderUserId == userId) {
				return new ResponseEntity<>("Cant sent request to yourself", HttpStatus.BAD_REQUEST);

			}

			userService.sendFriendRequest(userId, senderUserId);

			return new ResponseEntity<>("Friend request sent successfully", HttpStatus.OK);
		} catch (NumberFormatException e) {
			// Handle the case where the token doesn't contain a valid user ID
			return new ResponseEntity<>("Invalid sender ID in token", HttpStatus.UNAUTHORIZED);
		} catch (Exception e) {
			// Handle any other exceptions that might occur during the process
			return new ResponseEntity<>("Failed to send friend request: " + e.getMessage(),
					HttpStatus.INTERNAL_SERVER_ERROR);
		}

	}

//=====================get notifications=================================================
	@GetMapping("/notifications")
	public ResponseEntity<?> getNotifications(HttpServletRequest request) {
		String authHeader = request.getHeader("Authorization");
		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			return new ResponseEntity<>("Invalid User", HttpStatus.UNAUTHORIZED);
		}

		String token = authHeader.substring(7);
		String loggedInUserIdStr = jwtService.getUserIdFromToken(token);
		if (loggedInUserIdStr == null) {
			return new ResponseEntity<>("Invalid token", HttpStatus.UNAUTHORIZED);
		}

		try {
			long loggedInUserId = Long.parseLong(loggedInUserIdStr);
			Optional<User> user = userService.getUserById(loggedInUserId);

			if (user.isEmpty()) {
				return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
			}

			// Get all pending friend requests for this user (toUser = loggedInUser)
			List<FriendRequest> pendingRequests = friendRequestRepository.findByToUserAndStatus(user, "PENDING");

			// Convert request senders to UserProfileDTO
			List<UserProfileDTO> pendingSenders = pendingRequests.stream()
					.map(requestObj -> userService.getUserProfileById(requestObj.getFromUser().getId()))
					.collect(Collectors.toList());

			return new ResponseEntity<>(pendingSenders, HttpStatus.OK);

		} catch (NumberFormatException e) {
			return new ResponseEntity<>("Invalid user ID in token", HttpStatus.UNAUTHORIZED);
		} catch (UsernameNotFoundException e) {
			return new ResponseEntity<>("User not found", HttpStatus.NOT_FOUND);
		}
	}

	// =====================respond to friend request===================================================
	@PostMapping("/respond-request/{fromUserId}")
	public ResponseEntity<Map<String, String>> respondToRequest(@PathVariable Long fromUserId,
			@RequestParam boolean accept, @RequestHeader("Authorization") String authHeader) {
		Map<String, String> response = new HashMap<>();
		try {
			String jwt = authHeader.substring(7);
			String currentUsername = jwtService.extractUserName(jwt);

			userService.respondToFriendRequest(currentUsername, fromUserId, accept);

			response.put("message", "Response recorded.");
			return ResponseEntity.ok(response);

		} catch (RuntimeException e) {
			response.put("message", e.getMessage());
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
		}
	}
//=================home controller===========================================================

	@Autowired
	private PostService postService;


//=======================upload post===============================================	
	@PostMapping("/upload")
	public ResponseEntity<?> uploadPost(@RequestParam("image") MultipartFile image, Authentication auth) {

		try {

			if (auth == null)
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized User");

			String username = auth.getName();
			Post post = postService.uploadPost(image, username);
			return ResponseEntity.ok().body("Post Uploaded SuccessFully");
		} catch (IOException e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to upload image.");
		}
	}

//===========================get home page================================================================	
	@GetMapping("/home")
	public ResponseEntity<?> getHomePosts(Authentication auth) {
		if (auth == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized");
		}

		String username = auth.getName();
		User userEntity = userService.getUserByEmail(username);

		List<Post> posts = postService.getHomeAndOwnPosts(username);

		List<PostDTO> response = posts.stream().map(post -> {
			int likesCount = post.getLikes().size();
			boolean likedByCurrentUser = post.getLikes().stream()
					.anyMatch(like -> like.getUser().getId() == userEntity.getId());

			return new PostDTO(post.getUser().getUsername(), post.getImageUrl(),
					post.getPostTime() != null
							? Date.from(post.getPostTime().atZone(ZoneId.systemDefault()).toInstant())
							: null,
					post.getComments().stream().map(CommentDTO::new) 
																		
							.collect(Collectors.toList()), 
					likesCount, likedByCurrentUser);
		}).collect(Collectors.toList());

		return ResponseEntity.ok(response);
	}

	// =============================Comment Controller==============================================================================================================================

	@Autowired
	private CommentService commentService;

	// ======================add Comment============================================
	@PostMapping("/add/{postId}")
	public ResponseEntity<?> addComment(@PathVariable Long postId, @RequestBody Map<String, String> payload,
			Authentication authentication) {
		if (authentication == null)
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized user");

		String content = payload.get("text"); 
		String username = authentication.getName();

		try {
			Comment comment = commentService.addComment(postId, content, username);
			return ResponseEntity.ok(comment);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to add comment");
		}
	}

	// =========posts on profile(own posts)===========================================================
	@GetMapping("/my-posts")
	public ResponseEntity<?> getMyPosts(Principal principal) {
		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Session expired or user not authenticated.");
		}

		//=================Retrieve the authenticated user's email from the principal====================
		String email = principal.getName(); //=================principal.getName() contains the username/email

		//==================Fetch the posts by the user's email====================================
		List<Post> posts = postService.getPostsByEmail(email);

		if (posts.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("No posts found.");
		}

		return ResponseEntity.ok(posts);
	}

	// ================profile page======================================================================

	@GetMapping("/profile")
	@ResponseBody
	public ResponseEntity<?> getProfile(Authentication authentication, HttpSession session) {
		try {
			if (authentication == null || !authentication.isAuthenticated()) {
				return new ResponseEntity<>("Unauthorized", HttpStatus.UNAUTHORIZED);
			}

			String username = authentication.getName();
			UserProfileDTO userDTO = userService.getUserProfile(username);
			User userEntity = userService.getUserByEmail(username);

			List<UserFollow> followersList = userFollowRepository.findByFollowed(userEntity);
			List<UserFollow> followingList = userFollowRepository.findByFollower(userEntity);

			List<Map<String, String>> followers = followersList.stream()
					.map(f -> Map.of("username", f.getFollower().getUsername())).collect(Collectors.toList());

			List<Map<String, String>> following = followingList.stream()
					.map(f -> Map.of("username", f.getFollowed().getUsername())).collect(Collectors.toList());
			System.out.println("DEBUG: Followers List size = " + followersList.size());
			System.out.println("DEBUG: Following List size = " + followingList.size());

			//=========================Fetch the user's posts================================================
			List<Post> userPosts = postService.findByUser(userEntity);
			List<PostDTO> postDTOs = userPosts.stream().map(post -> {
				int likeCount = post.getLikes() != null ? post.getLikes().size() : 0;
				boolean likedByCurrentUser = post.getLikes() != null
						&& post.getLikes().stream().anyMatch(like -> like.getUser().getId() == userEntity.getId());

				//===========================Convert LocalDateTime to Date if not null=================================
				Date postTime = null;
				if (post.getPostTime() != null) {
					postTime = Date.from(post.getPostTime().atZone(ZoneId.systemDefault()).toInstant());
				}

				return new PostDTO(post.getUser().getUsername(), post.getImageUrl(), postTime,
						post.getComments().stream().map(CommentDTO::new)
																		
								.collect(Collectors.toList()),
						likeCount, likedByCurrentUser);
			}).collect(Collectors.toList());

			Map<String, Object> profile = new HashMap<>();
			profile.put("username", userDTO.getUsername());
			profile.put("profilePhotoUrl", userDTO.getProfilePhoto());
			profile.put("favSongs", userDTO.getFavSongs());
			profile.put("favBooks", userDTO.getFavBooks());
			profile.put("favPlaces", userDTO.getFavPlaces());
			profile.put("followers", followers);
			profile.put("following", following);
			profile.put("posts", postDTOs);

			Object loginTimeObj = session.getAttribute("loginTime");
			if (loginTimeObj instanceof LocalDateTime) {
				// Convert LocalDateTime to ISO 8601 string for robust parsing by JavaScript
				// Date object
				String isoLoginTime = ((LocalDateTime) loginTimeObj).atZone(ZoneId.systemDefault()) // Get the time in
																									// your system's
																									// default time zone
						.toInstant() // Convert to an Instant
						.toString(); // Format as ISO 8601 string (e.g., "2023-10-27T10:00:00Z")
				profile.put("loginTime", isoLoginTime);
				System.out.println("DEBUG: Login Time sent to frontend = " + isoLoginTime); // For server-side debugging
			} else if (loginTimeObj != null) {
				// Fallback or warning if loginTime is not LocalDateTime
				System.err.println(
						"WARNING: loginTime in session is of unexpected type: " + loginTimeObj.getClass().getName());
				// Still send the toString() representation, but the above is preferred
				profile.put("loginTime", loginTimeObj.toString());
			}

			return ResponseEntity.ok().contentType(MediaType.APPLICATION_JSON).body(profile);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Something went wrong");
		}
	}

	// ======================= Get Old Comments=========================================
	@GetMapping("/post/{postId}")
	public ResponseEntity<?> getComments(@PathVariable Long postId) {
		List<CommentDTO> comments = commentService.getCommentsByPost(postId);
		return ResponseEntity.ok(comments);
	}

	// ==========================like controllers====================================================
	@Autowired
	private LikeService likeService;

	// =======================like post========================================================
	@PostMapping("/like/{postId}")
	public ResponseEntity<?> likePost(@PathVariable Long postId, Principal principal) {
		if (principal == null) {
			return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
		}

		String username = principal.getName();
		System.out.println(username);
		Optional<User> optionalUser = userService.findByUsername(username);
		if (!optionalUser.isPresent()) {
			System.out.println(optionalUser.isPresent());
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
		}

		User user = optionalUser.get();
		System.out.println(user);
		System.out.println(postId);
		boolean liked = likeService.likePost(postId, user.getId());
		System.out.println("liked");
		if (liked) {
			return ResponseEntity.ok("Post Liked");
		} else {
			return ResponseEntity.status(400).body("Already liked the post");
		}
	}

//==============================unlike the post=============================================================
	@PostMapping("/unlike/{postId}")
	public ResponseEntity<?> unlikePost(@PathVariable Long postId, Principal principal) {
		try {
			if (principal == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not authenticated");
			}
			String username = principal.getName();
			System.out.println("Username Like on line 581 in authcont" + username);
			Optional<User> optionalUser = userService.findByUsername(username);

			if (!optionalUser.isPresent()) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
			}

			User user = optionalUser.get();
			System.out.println(user);
			Long userId = user.getId();
			System.out.println(userId);

			System.out.println(postId);
			boolean unlike = likeService.unlikePost(postId, userId);
			System.out.println(unlike);
			if (unlike) {
				return ResponseEntity.ok("Post unliked");
			} else {
				return ResponseEntity.status(400).body("You have not liked the post");
			}
		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred");
		}
	}

	// ====================count the number of likes===========================================================
	@GetMapping("/count/{postId}")
	public ResponseEntity<?> getLikesCount(@PathVariable Long postId) {
		long count = likeService.countLikesForPost(postId);
		return ResponseEntity.ok(count);
	}

	// =========================================Show posts of followings==========================================
	@GetMapping("/followings-posts")
	public ResponseEntity<List<PostDTO>> getFollowingsPosts() {
		try {
			// Get current logged-in user
			User currentUser = userService.getCurrentUser();
			if (currentUser == null) {
				return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
			}

			// Fetch posts from followings and from current user
			List<Post> followingsPosts = postService.getPostsByFollowings(currentUser.getId());
			List<Post> ownPosts = postService.getPostsByUser(currentUser.getId());

			// Combine all posts
			List<Post> combinedPosts = new ArrayList<>();
			combinedPosts.addAll(followingsPosts);
			combinedPosts.addAll(ownPosts);

			// Optional: sort posts by postTime in descending order
			combinedPosts.sort((p1, p2) -> p2.getPostTime().compareTo(p1.getPostTime()));

			// Convert to DTOs
			List<PostDTO> postDtos = combinedPosts.stream().map(post -> new PostDTO(post, currentUser.getId()))
					.collect(Collectors.toList());

			return ResponseEntity.ok(postDtos);

		} catch (Exception e) {
			e.printStackTrace();
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
		}
	}

}