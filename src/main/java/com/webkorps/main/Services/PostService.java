package com.webkorps.main.Services;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.webkorps.main.CustomExceptions.PostNotFoundException;
import com.webkorps.main.entity.Post;
import com.webkorps.main.entity.User;
import com.webkorps.main.entity.UserFollow;
import com.webkorps.main.repository.PostRepository;
import com.webkorps.main.repository.UserFollowRepository;
import com.webkorps.main.repository.UserRepository;

@Service
public class PostService {

    @Autowired
    private UserFollowRepository userFollowRepository;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserService userService;

    //===================================== Directory path where uploaded images will be stored==========================
    private final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/";

    //=========================== Upload a new post with an image=====================================================
    public Post uploadPost(MultipartFile image, String username) throws IOException {
        User user = userService.getUserByEmail(username);

        //========================== Save image file locally===================================================
        String fileName = System.currentTimeMillis() + "_" + image.getOriginalFilename();
        Path uploadPath = Paths.get(UPLOAD_DIR + fileName);
        Files.createDirectories(uploadPath.getParent());
        Files.write(uploadPath, image.getBytes());

        //============================== Set image URL relative to frontend====================================
        String imageUrl = "/uploads/" + fileName;

        // =================================Create Post object and save to DB===============================
        Post post = new Post();
        post.setUser(user);
        post.setImageUrl(imageUrl);
        post.setPostTime(LocalDateTime.now(ZoneId.of("Asia/Kolkata")));

        return postRepository.save(post);
    }

    //========================= Get posts made by users the given user is following (including self)==========================
    public List<Post> getPostsByFollowings(String username) {
        User user = userService.getUserByEmail(username);
        System.out.println("In Post service on line 59 : " + user);

        // Get followings
        List<User> followings = userFollowRepository.findByFollower(user)
                .stream()
                .map(UserFollow::getFollowed)
                .collect(Collectors.toList());
        System.out.println("In Post service on line 59 : " + followings);

        //============ Include user's own posts====================
        followings.add(user);

        //=================== Fetch posts in descending order of post time=========================
        return postRepository.findByUserInOrderByPostTimeDesc(followings);
    }

    //===============Get all posts of a specific user by username=============================
    public List<Post> getUserPosts(String username) {
        User user = userService.getUserByEmail(username);
        return postRepository.findByUserOrderByPostTimeDesc(user);
    }

    //========================Alias for getUserPosts (for current logged-in user)===========================================
    public List<Post> getMyPosts(String username) {
        return getUserPosts(username); //=======================reuse existing logic
    }

    //================================= Get single post by ID, or throw custom exception if not found===============================
    public Post getPostById(Long postId) {
        Optional<Post> post = postRepository.findById(postId);
        if (post.isPresent()) {
            return post.get();
        } else {
            throw new PostNotFoundException("Post not found with ID: " + postId);
        }
    }

    @Autowired
    private UserRepository userRepository;

    //===========================Get all posts of logged-in user and their followings=================================
    public List<Post> getHomeAndOwnPosts(String username) {
        //================= Find user by username=============================
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        System.out.println("in PostService on  line 107 : " + user);

        //===================== Get followings===========================================
        Set<User> followings = user.getFollowing();
        System.out.println("in PostService on  line 110 : " + followings);

        List<Post> posts = new ArrayList<>();

        //====================Get user's own posts====================================
        List<Post> ownPosts = postRepository.findByUser(user);
        System.out.println("in PostService on  line 116 : " + ownPosts);
        posts.addAll(ownPosts);

        //==================Add followings' posts==================================
        for (User following : followings) {
            posts.addAll(postRepository.findByUser(following));
        }

        //========================== Sort all posts by postTime in descending order=====================================
        posts.sort((a, b) -> b.getPostTime().compareTo(a.getPostTime()));

        return posts;
    }

    //========================Get all posts made by a specific user ID=============================================
    public List<Post> findByUserId(long id) {
        return postRepository.findByUserId(id);
    }

    //===========================Get all posts made by user using their email=======================
    public List<Post> getPostsByEmail(String email) {
        User user = userService.getUserByEmail(email);
        return postRepository.findByUserOrderByPostTimeDesc(user);
    }

    //===================== Get all posts made by a User object==================================
    public List<Post> findByUser(User user) {
        return postRepository.findByUser(user);
    }

    //=========================Get posts from a list of followings (with comments & likes if fetched via join)=============================
    public List<Post> getPostsOfFollowingsWithDetails(List<User> followings) {
        return postRepository.findPostsWithCommentsAndLikesByUsers(followings);
    }

    /*
    //  Get posts with comments & likes by a specific user
    public List<Post> getPostsOfUserWithDetails(User user) {
        return postRepository.findPostsWithCommentsAndLikesByUser(user);
    }

    //  Get posts with comments & likes by multiple user IDs
    public List<Post> findPostsByUserIds(List<Long> userIds) {
        return postRepository.findPostsWithCommentsAndLikesByUserIds(userIds);
    }
    */

    //================Get all posts made by followings of a user (by user ID), including self==================================
    public List<Post> getPostsByFollowings(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));

        List<User> followings = userFollowRepository.findByFollower(user)
                .stream()
                .map(UserFollow::getFollowed)
                .collect(Collectors.toList());

        followings.add(user); // Include self
        return postRepository.findByUserInOrderByPostTimeDesc(followings);
    }

    //======================Get all posts of a specific user by ID===================================
    public List<Post> getPostsByUser(long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with ID: " + userId));
        return postRepository.findByUserOrderByPostTimeDesc(user);
    }
}
