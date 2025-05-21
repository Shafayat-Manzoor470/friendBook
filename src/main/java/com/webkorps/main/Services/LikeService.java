package com.webkorps.main.Services;

import com.webkorps.main.entity.Like;
import com.webkorps.main.entity.Post;
import com.webkorps.main.entity.User;
import com.webkorps.main.repository.LikeRepository;
import com.webkorps.main.repository.PostRepository;
import com.webkorps.main.repository.UserRepository;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class LikeService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Autowired
    public LikeService(LikeRepository likeRepository, PostRepository postRepository, UserRepository userRepository) {
        this.likeRepository = likeRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    // Method to like a post
    public boolean likePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if the user has already liked the post
        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);
        if (existingLike.isPresent()) {
            // The post is already liked, do nothing or handle as un-like logic
            return false; // Already liked
        }

        // Create and save the like
        Like like = new Like();
        like.setPost(post);
        like.setUser(user);
        likeRepository.save(like);

        return true;
    }

    // Method to unlike a post
    public boolean unlikePost(Long postId, Long userId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        // Check if the user has liked the post
        Optional<Like> existingLike = likeRepository.findByUserAndPost(user, post);
        if (existingLike.isEmpty()) {
            // If no like exists, return false
            return false; // Not liked yet
        }

        // Delete the like
        likeRepository.delete(existingLike.get());
        return true;
    }

    // Count likes for a post
    public long countLikesForPost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new IllegalArgumentException("Post not found"));

        return likeRepository.countByPost(post);
    }
}
