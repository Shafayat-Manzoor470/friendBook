package com.webkorps.main.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.webkorps.main.entity.Like;
import com.webkorps.main.entity.Post;
import com.webkorps.main.entity.User;


@Repository
public interface LikeRepository   extends JpaRepository<Like, Long> {

	
	 // Check if the user has already liked the post
    Optional<Like> findByUserAndPost(User user, Post post);

    // Delete all likes for a post (for future purposes like removing all likes for a post)
    void deleteByPost(Post post);

    //  get all likes for a post (useful for counting likes on a post)
    long countByPost(Post post);
}
